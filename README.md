# Vault - registration, subscription and password organiser

# This repo is deprecated because of two reasons:

### 1. TornadoFX is no longer maintained and so upgrading this project in terms of Java and JavaFX became impossible
### 2. TornadoFX is a nice framework but I found a lot of subtle bugs which were super hard to debug due to it being a black box doing a lot of magic which is hard to look into and debug / understand.

####  It remains as a proof of concept of a personal Vault written in TornadoFX. The improved version ported to JavaFX ONLY can be found at: https://github.com/fast-reflexes/vault-javafx

Sensmoral av TornadoFx so far: buggigt ramverk, bättre att använda JavaFx helt och hållet tror jag

## Dokumentation:

* Använder Base64 för allt som har med krypto att göra, använder UTF8 för att läsa och skriva till fil (eftersom jag 
har radbrytningar där så Base6 räcker ej) samt UTF8 i den okrypterade versionen av valvet (när den krypteras blir den 
till Base64).
* Tänkte använda GCM för att kryptera men insåg att detta ej var bra då GCM är mer känsligt än CBC. Med GCM hade jag 
behövt hålla reda på vilken IV som användes varje gång och det ger ett probem, nämligen att en attackerare kan spara en 
gammal fil (med en tidigare IV) och låta en användare använda den igen (varvid 2 olika krypteringar med samma IV / Nonce 
kan erhållas vilket breakar hela confifentialityn. Dessutom skulle jag även MED GCM beöva köra en HMAC på hela den 
sparade fieln för at se att inte anat har förändrats.. Dena mac innehåller även ciphertexten vilket ger en 
Encryot-then-MAC-skydd till CBCn... Detta är tillräckligt varför den extra autenticering som GCM bringar till tablen 
är onödig. an hade kunnat använda en randomiserad IV för GCM (är ok med 128 bitar) men återigen finns det ingen vinst 
med det.. GCM är fett men i detta fall är CBC bättre).

## Kunskap

### TornadoFX:

#### Fragment:

Som View men finns flera olika och de undockar så fort de inte används någonstans. När man kör ett sånt med find så 
skapas ett nytt objekt.

#### `openDialog, openWindow, openInternalWindow`
		
`openDialog` öppnar en dialog som blockerar huvud-UIt tills den är stängd, `openWindow` öppnar ett nytt fristående 
fönster medan `openInternalWindow` öppnar ett fönster I det befintliga fönstret ÖVER det och blockerar dess innehåll.


#### Byt view
	
Man kan byta view på flera sätt:
* om man vill byta den vy man befinner sig inom kör man: `replaceWith<NewView>()`
* om man vill byta en ANNAN vy kör man: `find<OtherView>().replaceWith<NewView>()` eller `positionOfOtherView = find<NewView>().root`

#### Förmedla data till annan vy:

Detta går att göra på flera sätt:
* Skapa ett SCOPE och stoppa något i scopet i find()-anropet, find skapar eller hittar nämligen en vy för VARJE SCOPE (alltid globalscope om inget annat scope sätts) och då kan man injicera det man vill ha i scopet
* Genom att ge parametrar till vyn (dessa uppdateras i vyn om parametern uppdateras)
* Genom att använda fragment istället för View (där skapas nytt varje gång) (dock måste man även här använda parametrar eller injicering).)
* Genom controller (sätt parametrar i controller och låt sedan den andra vyn få controllern injicerad)
					
Jag har kommit fram till följande förhållningssätt:
* Använd fragments för väldigt små UI-enheter med lätthanterlig data
* Använd controller då mer omfattande data används i den andra vyn
* Använd scope då större UI-bitar som ser likadana ut funktionellt ska existera samtidigt men behöver strikt åtskilda världar
* Använd parametrar för data som mestadels ska displayas och som inte ska INTERAGERAS med.. ska interaktion ske behövs ändå en controller och då kan man lika gärna förvara datan där
		
#### Frågor:
* Hur får man i onDock en View att sätta rätt storlek till sig (som i sizeToScene = true när anropet görs)?
* Finns det något sätt att se när en View har förstörts, gått utom scope helt?
  
## Frågor och svar

### Hur funkar konversion till 7-bit ASCII tex? Vad görs med den sista biten?
ALLA bytearrayer som omvandlas till en sträng genom new String(bytes, Charset) har automatiskt en replacement character 
för bytes som inte finns i deras representation. Vill man ha mer kontroll över detta så får man använda en 
CharacterEncoder. Detta innebär att i krypteringsavseende är det bra om man ser till att det Charset man använder kan 
hantera allt tänkbart bytesinput som man skickar in, eftersom annars begränsas säkerheten genom att OLIKA bytes mappas 
till SAMMA karaktär vilket är ofördelaktigt. Kom också ihåg att även tecken som inte SYNS faktiskt ÄR tecken och lagras 
som sådana i en sträng. Ska strängen användas för I7O av en människa finns det dock såklart en begränsning här.

### Varför visas automatiskt komponenter i variabler som deklarerats (men ej lagts till) efter root-elemnetet i TornadoFX?
Builders, dvs typ det som TornadoFX egentligen bidrar med i störst grad, fäster automatiskt till den plats de är 
deklarerade. Det innebär att de fäster till vyn men då man deklarerar root så skriver man om vyns grund och därmed syns 
inte det man tidigare deklarerat. Om man lägger de efter root syns de däremot eftersom de fäster på samma objekt som den 
root man angett innehåller.

Följande är råden:
1. Gör allt med builders, man ska inte behöva deklarera element utanför.
2. Måste du ha en referens till ett element, deklarera variablen med singleAssign() och assigna när du gör elementet.
3. Måste du göra elementet utanför hierarkin, använd inte builders utan JavaFX-klasser istället.

## Länkar:

* TornadoFX:
  * Official sources:
    * Website:
      * https://tornadofx.io/
    * TornadoFX docs:
      * https://github.com/edvin/tornadofx-guide (samma innehåll som https://edvin.gitbooks.io/tornadofx-guide/ som tyvärr är onåbar hemifrån oss av ngn anledning)
      * https://github.com/edvin/tornadofx/wiki
    * Package overview:    
      * https://tornadofx.io/dokka/tornadofx/tornadofx/      
    * Source code:
      * https://github.com/edvin/tornadofx/tree/master/src/main/java/tornadofx    
  * Initial tutorials used:  
    * https://www.kotlindevelopment.com/super-productive-native/

## TODO

* Comment is not updated after writing it but it's still there when reopening the app
* Add button when looking at credentials that forces you to manually expose the password
* When you add the password, also add it with asterisks except if a checkbox is filled indicating clear text
* Comment section does not wrap like it should when comment stretches wider than the width of the field
* Looses focus when saving an entry (must click it again to continue seeing it)
* Remove horizontal scroll on identifiers overview
* Scroll position i Y is not reset in VAULT ENTRIES list upon relogin
* Kolla att den sparar lösen när man precis loggat in, den verkar begära nytt lösen direkt typ 
* MainView filterList() - Lägg till för stöd för att söka specifikt (just nu kollas bara main identifier)

* gör funktion så att man kan slippa logga in inom angiven tidsgräns
* hantera last updated i credentials (och i entry?)
* credentials i controller eller som param?
* kolla att stängning med kryss och cancel ger samma resultat ( påmminner när något är osparat etc...)
* entry avselecteras om man uppdaterar ett entry, why? det är den vanliga buggen som jag såg i början... att 
associationproxy ibland blir null när man sparas... skapa buggexempel eller ta reda på vad som ör fel
* filterfunktion ska funka korrekt
* fixa sjysst meny
* fixa ikon
* om man klickar på filterrutan flera ggr när man startar ändras storleken, swhy?
* set min storlekar på alla fönster så att man inte kan förminska under det
* skapa setings sida där man kan ändra masterpassword, slumpa fram en sträng, ev. fler saker
* ändra button text på en del ställen till "Quit anyways" eller "Cose anyways" istället för "Ok"
* kolla beteende med när man byter entry och stänger utan att ha sparat å så så att de varningar jag vill ha upp kommer upp som de ska
* vad består egentligen en separator av? Varför går den inte att sätta ordnetligt (färg etc..)
* fult flimmer i hljdled när filter öppnas och stängs
* vd e currentstage och prikarystage?
* returnerar man ett värde cleant från View eller Fragment? Helst via en inbyggd metod motsvarande den som finns på Dialog (kolla https://github.com/edvin/tornadofx/issues/226)
* varför fel på gradle när man startar nytt projekt med template?
* hur testar man enskilda vyer i JavaFX och TornadoFX? Man kan ju inte behöva starta om hela skiten varje gång!
* hur görs översättning till ascii om fel bytes används? Säkert
* varför konstigt fel där commit ibland gör rollback? Håll koll på det här! Har provat med både binding och listbinding 
en det har upträtt i sllsynta fall alltid medbindings.. nu kör jag bara listener tror att felet är i itemviewmodel
* to bort association i så hög grad som möjligt och kör bara direkt till associationmodel	
* tänk på filtrering och hur göra med isNeeded tex.. ska frånvaro av den betyda att man bafa vill ha såna där den INTE är på eller vad?	
* Kanske göra tableview ändå med kategori i kolumn 2 och att man kan sortera efter den?
* refaktorera så att et finns fler olika views	
* hur injicera model?	
* gå igenom allt och om jag verkligen gör saker i rätt ordning	
* kolla att regex funkar.. verkar inte som det just nu	
* skriv klart filterfunktionenr
