# ArtemMod: Рука Зевса

Мод под Minecraft Java 1.21.10 + Fabric.

## Что добавляет

- Предмет `Рука Зевса` / `artemmod:zeus_hand`
- ПКМ вызывает молнию в точку, куда смотрит игрок
- Кулдаун: 2 секунды
- Прочность: 128 использований
- Иконка предмета уже внутри ресурсов
- Рецепт: золото + blaze rod + алмаз

## Как собрать

Нужна Java 21 и Gradle.

```bash
gradle build
```

Готовый `.jar` появится здесь:

```text
build/libs/artemmod-hand-of-zeus-1.0.0.jar
```

Его нужно положить в `.minecraft/mods` вместе с Fabric API для Minecraft 1.21.10.

## Команда выдачи

```mcfunction
/give @p artemmod:zeus_hand
```
