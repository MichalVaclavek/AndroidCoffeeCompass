# CoffeeCompass

CoffeeCompass je Android aplikace navržená pro vyhledávání a správu kávových míst (coffee sites). Umožňuje uživatelům hledat kavárny v okolí, zobrazovat jejich detaily, hodnotit je a přispívat přidáváním nových míst.

## Hlavní funkce
- **Vyhledávání kávových míst:** Hledání nejbližších míst pomocí polohových služeb, zobrazení na mapě nebo v seznamu.
- **Detailní informace:** Podrobné informace o každém místě, včetně hodnocení, komentářů a obrázků.
- **Uživatelské účty:** Registrace, přihlášení, správa uživatelského profilu.
- **Hodnocení a komentáře:** Uživatelé mohou přidávat hvězdičková hodnocení a psát recenze.
- **Přidávání obsahu:** Možnost vytvářet nová kávová místa nebo upravovat stávající (pro přihlášené uživatele).
- **Offline režim:** Podpora pro práci s daty i bez připojení k internetu.
- **Notifikace:** Integrace s Firebase Cloud Messaging pro odebírání novinek.
- **Widget:** Widget na domovskou obrazovku pro rychlý přehled blízkých míst.

## Použité technologie
- **Jazyk:** Java (Android Native)
- **Sítě:** Retrofit pro komunikaci s REST API.
- **Mapy:** Google Maps SDK.
- **Lokální databáze:** Room Persistence Library.
- **UI Binding:** ButterKnife.
- **Načítání obrázků:** Picasso.
- **Push notifikace:** Firebase Cloud Messaging (FCM).
- **UI:** Material Components.

## Struktura projektu
- `cz.fungisoft.coffeecompass2.activity`: UI aktivity pro navigaci, mapy, detaily a správu účtu.
- `cz.fungisoft.coffeecompass2.services`: Služby na pozadí pro polohu, synchronizaci dat a Firebase.
- `cz.fungisoft.coffeecompass2.asynctask`: Asynchronní úlohy pro síťové a databázové operace.
- `cz.fungisoft.coffeecompass2.widgets`: Implementace aplikačních widgetů.
