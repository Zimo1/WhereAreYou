# Проект “Где ты?” (“Where are you?”)

Приложение, предназначенное для обмена данными о местоположении (локациях) пользователей посредством SMS в условиях неустойчивой мобильной связи.

Проект находится в разработке!

Основные задачи:
- Получение текущих GPS-координат и отображение своего местоположения на карте.
-	Формирование и посылка SMS с данными о своем местоположении выбранному абоненту, либо запрос его местоположения.
-	Автоматический прием SMS-сообщений, отображение полученных данных о местоположении абонента на карте вместе со своим местоположением. Вычисление расстояния по прямой между этими двумя точками.
-	Сохранение и просмотр местоположений в базе данных (в перспективе).

Приложение имеет 2 точки входа, используются 2 разных Activity: 
1) Обычная, приложение запускается при помощи ярлыка в Launcher.
2) Через постоянный Broadcast Receiver, который отслеживает поступление всех SMS на смартфон. Чтобы отличить SMS, созданные приложением, от прочих, у всех входящих SMS проверяется наличие заданного заголовка. Если заголовок присутствует, формируется нотификация (Notification), содержащая Pending Intent на запуск Activity (в связи с новой политикой безопасности Google прямой запуск Activity из background заблокирован).

Сообщения, передаваемые в SMS, могут быть следующих типов:
1) REQUEST - запрос локации у абонента, без передачи ему данных своей локации.
2) FULL_REQUEST - запрос локации у абонента, содержащий в себе локацию запрашивающего.
3) ANSWER - ответ на запрос локации абонентом.
4) INFO - сведения о своей локации, передаваемые абоненту без запроса с его стороны.

Кроме данных о местоположении в сообщение может быть включена и другая сопутствующая информация, например текстовый комментарий абоненту, уточняющий смысл запроса; уровень зарядки смартфона и т.п.

Технологический стек:
-	Kotlin
-	Android Architecture Components
-	View Binding
-	Navigation Architecture Component
-	API: Permissions, Location, SmsManager, Notifications, Google Maps.

У приложения 2 локали: русская и английская (по умолчанию).

При создании приложения поставлена цель осуществления как можно большей независимости от Google Play Services, поддерживаемого не на всех моделях смартфонов и не во всех странах мира. Таким образом:
- Работа с GPS осуществляется при помощи API Location, а не Fused Location.
- Работа с SMS производится посредством SmsManager, а не SMS Retriever API.

Единственным сервисом, зависимым от Google Play Services, в этом приложении являются карты Google, от использования которых планируется отказаться на следующих этапах.

---
NB:
1) Чтобы карта показывалась на экране, необходимо иметь свой ключ API Google Maps (генерируется в консоли Google). Его нужно поместить в файл res\values\google_maps_api.xml. Данные для генерации ключа: 
- Package name: ru.yodata.whereareyou
- SHA-1 certificate fingerprint: на каждом компьютере будет свой. Его можно взять из логов сообщения об ошибке, если запустить приложение с текущим ключом.

При этом даже неработающая карта не мешает отправлять данные о своем местоположении.

2) На смартфонах Xiaomi и возможно других китайских необходимо вручную дать приложению "Where Are You" разрешение на автозапуск (Настройки -> Приложения -> Все приложения -> Where Are You -> Автозапуск). Без этого приложение не сможет автоматически запускаться при получении SMS, т.к. у этих производителей своя политика безопасности.

