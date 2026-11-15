# Tool Wheel (Satisfactory-style Radial Tool Menu)

A lightweight NeoForge mod for Minecraft 1.21.1 inspired by the Satisfactory tool wheel. It allows players to quickly switch between essential tools (such as wrenches, lifts, welders, and building tools) via a radial menu without cluttering the inventory.

---

## English

### Features

- Phantom Hand Slot: Tools selected from the radial menu are placed directly in the main hand without taking up any of the 36 inventory slots. Your original item is safely stored internally.
- Zero Inventory Clutter: Works even when your inventory is 100% full. No items are displaced or dropped on the ground.
- Automatic Return: The virtual tool dissolves and your original item is restored whenever you:
  - Switch hotbar slots (scroll wheel or keys 1-9)
  - Open any container or inventory screen (cheats, backpacks, crafting tables)
  - Press the drop key (Q)
  - Select the center cancel button in the radial menu
  - Die or disconnect
- Full Mod Compatibility: Tools held in hand are real ItemStacks at runtime, meaning mod interactions (Create wrench rotation, mechanic sounds, durability, right-click actions) work natively out of the box.
- Dynamic Configuration: Tools are loaded from `config/satisfactory_wheel.json`. The mod generates a default template on first run.
- In-Game Reload: Reload changes on the fly using `/toolwheel reload` without restarting the game.

### Controls

- Open Wheel: Hold `R` (configurable in Controls -> Key Binds -> Satisfactory Tool Wheel).
- Select: Move mouse towards desired tool and release `R`.
- Holster / Cancel: Move mouse to center cross and release `R`.
- Toggle Mode: Set `"hold_to_select": false` in the config to open/close by tapping the key instead of holding.

### Configuration Example (`config/satisfactory_wheel.json`)

```json
{
  "hold_to_select": true,
  "wheel_radius": 55,
  "tools": [
    {
      "id": "create:wrench",
      "name": "Wrench"
    },
    {
      "id": "create_lnl:contraption_lift",
      "name": "Contraption Lift"
    },
    {
      "id": "create_lnl:physic_welder",
      "name": "Physic Welder"
    }
  ]
}
```

---

## Русский

### Особенности

- Эфемерный слот руки: Выбранный из радиального меню инструмент помещается в руку, не занимая ни одного из 36 слотов инвентаря. Исходный предмет бережно сохраняется во внутреннем хранилище.
- Никакого засорения инвентаря: Работает даже при полностью забитом инвентаре (36/36). Предметы не выпадают под ноги и не перемешиваются.
- Бесшовный возврат: Виртуальный инструмент растворяется, а родной предмет мгновенно возвращается в руку, если вы:
  - Прокрутили колесико мыши или нажали клавиши 1-9
  - Открыли любой инвентарь или контейнер (сундук, верстак, рюкзак)
  - Нажали клавишу выброса предмета (Q)
  - Выбрали центральный крестик в радиальном меню
  - Погибли или вышли из игры
- Полная совместимость: Инструмент в руке является полноценным ItemStack во время использования, поэтому анимации, звуки и взаимодействия модов (вращение ключом Create, сварка и т.д.) работают штатно.
- Динамический конфиг: Список предметов загружается из `config/satisfactory_wheel.json`. При первом запуске создается шаблон.
- Горячая перезагрузка: Команда `/toolwheel reload` обновляет список инструментов без перезапуска игры.

### Управление

- Открыть колесо: Зажать `R` (настраивается в Настройки -> Управление -> Назначение клавиш).
- Выбрать: Навести курсор на нужный сектор и отпустить `R`.
- Убрать инструмент: Навести курсор на центральный крестик и отпустить `R`.
- Режим переключения: Установите `"hold_to_select": false` в конфиге, чтобы меню открывалось по нажатию, а не по удержанию.

---

## Building from Source

Requirements: Java 21 JDK and NeoForge 21.1.256.

## License

Public Domain / Unlicense.
