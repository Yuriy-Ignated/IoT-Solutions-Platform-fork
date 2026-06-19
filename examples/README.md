# Examples

## terminal-app

Пример **application bundle** для проекта terminal (michaael) по REQ-PF-03.

```
terminal-app/
  manifest.yaml
  data/migrations/     # SQL таблицы приложения (не Flyway платформы)
  functions/           # JSON script-функции
```

Деплой:

```http
POST /api/v1/applications/terminal/deploy
```

Соберите тело из `manifest.yaml`, миграций и функций — см. [docs/APPLICATIONS.md](../docs/APPLICATIONS.md).

Java reference-стенд нефтебазы — ветка `feature/oil-terminal-reference`, не в `main`.
