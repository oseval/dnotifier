# Datahub

Библиотечка предназначена для бесшовного доступа к данным, источник которых расположен снаружи системы. 
Например если источником данных служит (микро)сервис, то другие (микро)сервисы могут иметь прозрачный доступ к этим данным.
То есть фактически иметь эти данные локально в памяти, без необходимости поддерживать их консистентность и/или инвалидировать кеш.

## Концепция:
Для оперирования распределенными данными обычно используют такие концепции как *CRDT, ORDT, RAFT, Vector clock, Version vectors*.
Однако все они подразумевают, что данные имеют более одного источника и таким образом всегда должна быть некая функция **merge**, для разрешения неизбежных конфликтов, 
которая в большинстве случаев должна быть написана руками (при этом бывает довольно сложно доказать, что она **коммутативна**), кроме того в общем случае невозможно гарантировать, 
что данные **целостны** на определенном временном отрезке. 

Зачастую же бывает, что хотя, данные представлены в виде *CRDT* (или его эмпирическго подобия), но имеют один источник данных,
и представлены таким образом только потому что от источника к потребителю могут попадать разными **асинхронными** путями *(по RPC, websocket, eventbus, из бызы данных)*.
В таком случае *CRDT* с одной стороны избыточно, а с другой, не использует все гарантии единого источника данных. Такие как **линейность** и отсутствие конфликтов в принципе.

В таких случаях (а их большинство) логично использовать слегка иной концепт данных.

Идея такова:
- Любые данные можно представить в виде функции от времени. *d = D(t)*. 
- Данные одного источника меняются в определенные моменты времени: *zero = D(0), d1 = D(t1), d2 = D(t2),.. current = D(now)*
- То есть любое изменение данных (*update*), можно представить в виде интервала от одного состояния к другому *d1_2 = Di(t1, t2) = D(t2) - D(t1)*.
- При наличии у потребителя информации об интервалах, имеющихся у него данных, всегда можно понять есть ли пропущенные интервалы или нет (и таким образом узнать о необходимости дополнительной синхронизации).
- Для синхронизации данных необходимо и достаточно запросить у их источника недостающие интервалы.
- Зачастую *update'ы* данных не сохраняются как есть, а применяются к существующей модели и лишь изменяют ее, не увеличивая объем данных. При этом информация о самом *update'е* теряется. Поэтому для форирования недостающего интервала источник данных должен иметь возможность сформировать новый *update*, со всеми недостающими изменениями. *D(t1, t3) = D(t1, t2) + D(t2, t3)*. Для чего данные должны быть **ассоциативны**.
- Данные деляться на два подтипа:
  - *Атомарные* данные. Update'ы данных такого типа передаются целиком: `User(name = "Вася")` -> `User(name = "Вася Курочкин")`. И в таком случае *D(t2) = D(t2) - D(t1)*. То есть для каждого *update'а* достаточно содержать лишь конец интервала. 
  - *Коллекции*. Большие, а также условно "бесконечные" коллекции невозможно передавать в каждом *update'е*, поэтому каждый из них должен содержать как начало, так и конец интервала.

И того у нас появляются несколько сущностей:
- `Data` - собственно данные
- `Entity` - сущность данных, то есть фактически все, что имеет *id* в том или ином виде. *UserId(123), GroupId("tratata"), SingletonName("global_data")*
- `Datasource` - собственно источник данных, который предоставляет нам методы для получения/обновления данных.
- `Subscriber` - потребитель данных
- `Datahub` - "синглтон" в пределах микросервиса (или точнее ноды), роутер который отвечает за подписки на источники данных.
- `LocalDataStorage` - абстракция, предоставлящая синхронный интерфейс для прозрачного доступа к любым данным.

## Подробнее о данных 

### Data

В данных примерах `clock` всегда `Long = unix_timestamp`

```
trait Data {
  val clock: Long
}
```

* `clock` - одновременно **уникальный id** и **timestamp**, необходим для гарантирования данным коммутативности и идемпотентности.

```
trait AtLeastOnceData extends Data {
  val previousClock: Long
  def isSolid: Boolean
}
```

* `previousClock` - призван гарантировать **целостность** данных (дает возможность автоматически определять потерю данных в процессе передачи).
* `isSolid` - собственно флажок, показывающий **целостность** данных.

### Entity

```
trait Entity {
  val kind: String
  val entityId: String
}
```

* `kind` - вид данных (*User*, *Group*..)
* `entityId` - *id* конкретной сущности (*User(123)*, *Group("admins")*..)

### DataSource

```
trait Datasource {
  val entity: Entity
  def syncData(dataClock: Long): Unit
}
```

* `entity` - источником изменений какой именно сущности является данный источник
* `syncData` - функция, необходимая для сигнализации источнику о потере некоторых данных потребителем и необходимости публикации изменений данных произошедших с момента `dataСlock`.

### Subscriber

```
trait Subscriber {
  def onUpdate(relation: Entity, relationData: Data): Unit
}
```

* `onUpdate` - функция для сигнализации потребителю о публикации источником апдейта данных.

### Datahub

 > *В большинстве случаев хаб не должен использоваться напрямую, а лишь посредством LocalDataStorage*

```
trait Datahub {
  def register(source: Datasource): Unit
  def subscribe(entity: Entity,
                subscriber: Subscriber,
                lastKnownDataClock: Any): Boolean
  def unsubscribe(entity: Entity, subscriber: Subscriber): Unit
  def dataUpdated(entity: Entity, data: Data): Unit
  def syncRelationClocks(subscriber: Subscriber, relationClocks: Map[Entity, Any]): Unit
}
```

* `register` - регистрирует источник данных
* `subscribe` - подписывает потребителя на обновления источника данных для конкретной сущности, начиная с `lastKnownDataClock`
* `unsubscribe` - отписывает потребителя от обновлений
* `dataUpdated` - публикует обновление данных источника для рассылки всем потребителям
* `syncRelationClocks` - испольуется для получения обновлений всех источников, необходимых данному потребителю