# Лицензия и границы поставки

## Что входит в «ядро» (ветка `main`)

**IoT Solutions Platform Framework (ISPF)** — открытое ядро платформы:

| Путь | Лицензия |
|------|----------|
| `packages/ispf-*` | [MIT](../LICENSE) |
| `apps/web-console/` (платформенная консоль) | [MIT](../LICENSE) |
| `docs/` | [MIT](../LICENSE) |

Copyright: **© 2026 ISPF Core Contributors** (см. [LICENSE](../LICENSE)).

Ядро можно свободно использовать, изменять и распространять (включая коммерческие продукты) при сохранении текста MIT-лицензии.

## Что не входит в ядро

Следующее **не должно** попадать в `main` и **не** покрывается MIT ядра:

| Тип | Где живёт | Лицензия |
|-----|-----------|----------|
| Отраслевые reference-стенды (нефтебаза P-301, oil-terminal) | Ветка `feature/oil-terminal-reference` | По условиям ветки / отдельный EULA |
| Коммерческие плагины и прикладные решения | Отдельные репозитории или ветки | **Явно указана** в `LICENSE` / `license.json` / README плагина |
| App bundle конкретного заказчика (michaael, terminal) | Репозиторий проекта, не framework | По договору с заказчиком |

Коммерческий плагин обязан содержать **собственный** файл лицензии или `license.json` с явным типом (commercial, trial, и т.д.). Ядро загружает и исполняет расширения через REQ-PF (REST deploy, functions), но **не включает** их исходники в свой MIT-дистрибутив.

## Политика веток

```
main                          → только MIT-ядро платформы
feature/oil-terminal-reference → reference oil-terminal (не merge в main)
<plugin-repo>                 → коммерческий или открытый плагин со своей лицензией
```

Pull request в `main` с кодом отраслевого приложения (terminal, oil-terminal, заказные BFF) **отклоняется** — переносите в отдельную ветку или репозиторий.

## Обязательства при распространении ядра

1. Сохраните [LICENSE](../LICENSE) и copyright notice.
2. Приложите [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) к бинарным сборкам.
3. Соблюдайте условия зависимостей (особенно **bpmn-js** — watermark, см. ниже).

## Зависимости третьих сторон

MIT распространяется **только на код ISPF**, не на весь стек. Подробный список: [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

Критично для Web Console:

- **bpmn-js** — лицензия bpmn.io: watermark `bpmn.io` на диаграммах **нельзя** удалять или перекрывать; для white-label нужна отдельная лицензия у Camunda.

## Отказ от гарантий

Как в [LICENSE](../LICENSE): ПО «как есть», без гарантий.

## Связанные документы

- [PLUGINS.md](PLUGINS.md) — куда класть плагины и reference-стенды
- [APPLICATIONS.md](APPLICATIONS.md) — REQ-PF API для deploy приложений **вне** ядра
- [ARCHITECTURE.md](ARCHITECTURE.md) — reference stands
