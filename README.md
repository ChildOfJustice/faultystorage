# Миграция данных в Faulty Storage
[Faulty Storage](https://teamcity.jetbrains.com/repository/download/TeamCityPluginsByJetBrains_Unsorted_FaultyStorage_BuildServer/.lastSuccessful/faulty-server.jar?guest=true) – это бедовый сервер, но другого в некоторой воображаемой компании Faulty Inc., к сожалению, не предвидится, так что сегодня придётся поработать с таким.

По ссылке выше находится jar-файл Faulty-Storage сервера. Приложение написано на восьмой java, поэтому для того, чтобы его запустить, нужно установить jdk версии не меньше 1.8. После запуска командой
```bash
# java -jar faulty-server.jar
```
приложение начинает слушать http-реквесты по адресу `http://localhost:8080`.

## API Faulty Storage

OpenAPI документация (описание доступных URL'ов) после старта приложения доступна [по ссылке](http://localhost:8080/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config), но для удобства она продублирована ниже.

### API Нового хранилища

#### GET <base-url>/storage/files возвращает список файлов в новом хранилище
#### GET <base-url>/storage/files/{filename} возвращает контент файла с указанным именем
#### DELETE <base-url>/storage/files/{filename} удаляет файл с указанным именем
#### POST <base-url>/storage/files multipart/form-data загружает файл в новый storage 

## Задача
Вам нужно, не меняя API сервера, переписать код так, чтобы загружаемые файлы хранились не на сервере, а в AWS S3.

В результате у вас должен получиться fork этого репозитория с решением задачи и E2E тестами. Мы ждём от вас ссылку на github с кодом выполненного задания и, конечно, желательно, чтобы у нас была возможность собрать приложение из этого исходного кода и запустить E2E тесты с минимальной и понятной конфигурацией.
