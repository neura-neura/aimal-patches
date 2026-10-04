# Personalización de subtítulos en Aimal

Este fork agrega un apartado **SUBTÍTULOS** al reproductor. Su vista previa y los
subtítulos durante la reproducción usan el mismo renderizador. Los cambios se
guardan al mover los controles; no hay un botón de aplicar ni hace falta cambiar
de episodio o de idioma. También se puede desactivar el estilo personalizado y
recuperar inmediatamente el dibujo original de la aplicación.

La pastilla **FIT / SUBTÍTULOS** aparece junto con los controles del reproductor
y desaparece por completo cuando estos se ocultan. Para recuperarla, muestra
los controles normalmente: con un toque en el móvil o con el mando al usar TV.
Los dos botones admiten foco y activación con D-pad y muestran el foco claramente.
La navegación con D-pad se comprueba en emulador; esto no constituye un port
del APK de Crunchyroll para Android TV.

La referencia es el esquema `SubtitleStyle` de
[Noir Player, revisión 54eb8d1](https://github.com/neura-neura/noir-player/blob/54eb8d176475186580259686050b8f4bec141aeb/src/App.tsx#L97).
Las 15 propiedades están incluidas:

| Propiedad de Noir | Control | Intervalo |
| --- | --- | --- |
| `fontFamily` | Familia instalada, búsqueda, paginación, ruta local o importación CSS | Fuentes disponibles en Android y fuentes importadas |
| `fontSize` | Tamaño | 8–200 px |
| `fontWeight` | Peso | 100–900 |
| `textColor` | Color del texto | `#RRGGBB` |
| `backgroundColor` | Color del fondo | `#RRGGBB` |
| `backgroundOpacity` | Opacidad del fondo | 0–100 % |
| `bottomOffset` | Distancia desde abajo | 0–100 % |
| `useCustomMaxWidth` | Limitar ancho máximo | Activado/desactivado |
| `maxWidth` | Ancho máximo | 10–100 % |
| `paddingX` | Margen interno horizontal | 0–80 px |
| `paddingY` | Margen interno vertical | 0–80 px |
| `borderRadius` | Radio de las esquinas | 0–80 px |
| `lineHeight` | Interlineado | 0,5–3 |
| `letterSpacing` | Espaciado entre letras | −5–10 px |
| `textShadow` | Sombra del texto | Activado/desactivado |

Todos los controles numéricos permiten deslizar o escribir un valor exacto.
**Restablecer estilo** recupera los valores iniciales. La fuente inicial es
`sans-serif`, disponible en Android; **Cargar GothamPro de Noir** importa la
familia de la referencia. Los píxeles son píxeles de pantalla Android: el tamaño
se conserva al cambiar el ancho, y cambia el salto de línea. Los porcentajes se
calculan sobre el área real de subtítulos.

## Instalar con Morphe Manager

1. Abre [Agregar este fork como fuente](https://morphe.software/add-source?github=neura-neura/aimal-patches).
   También puedes pegar `https://github.com/neura-neura/aimal-patches` en
   **Fuentes de parches → Agregar fuente**.
2. Actualiza la fuente y comprueba que muestra **1.2.0** o una versión posterior
   de este fork. Evita seleccionar también la fuente original de Aimal para la
   misma aplicación: ambos paquetes modifican los mismos métodos.
3. Selecciona tu APK/APKM original. Las versiones de referencia del proyecto son
   Crunchyroll **3.117.0**, HBO Max **7.9.0.84**, Disney+
   **26.14.1+rc2-2026.08.20** y Viki **26.5.0**. HBO Max usa `com.wbd.stream`.
4. Para Crunchyroll, activa **Subtitle styling**; incluye la dependencia
   **Aspect ratio control**. Para las otras aplicaciones, activa
   **Playback speed and aspect ratio**.
5. Aplica los parches e instala la aplicación resultante. Si Android rechaza
   la firma porque está instalada la versión oficial, guarda tus datos antes
   de desinstalarla e instalar la versión parcheada.
6. Reproduce un vídeo con una pista de subtítulos de texto seleccionada. Toca
   **SUBTÍTULOS**, cambia el tamaño o el color y comprueba tanto la vista previa
   como los subtítulos reales. Pausa sobre una frase para comprobar el cambio
   sin esperar al siguiente subtítulo.

Como alternativa, descarga el `.mpp` desde
[Releases](https://github.com/neura-neura/aimal-patches/releases/latest) e impórtalo
como paquete local en Morphe Manager.

## Instalar con Morphe Desktop

```powershell
java -jar morphe-desktop-1.18.0-all.jar patch -p https://github.com/neura-neura/aimal-patches app.apkm
```

También se puede usar `-p patches-1.2.0.mpp` para el archivo descargado. Manager
y Desktop admiten la combinación de los paquetes divididos de las aplicaciones.
Consulta la documentación oficial de
[fuentes](https://github.com/MorpheApp/morphe-manager/blob/main/docs/patch-sources.md)
y [Desktop](https://github.com/MorpheApp/morphe-desktop/blob/main/docs/documentation.md).

## Fuentes y subtítulos que tienen límites

La lista de familias instaladas refleja Android, no las fuentes de Windows.
La importación CSS descarga fuentes TTF/OTF por HTTPS, las verifica con
`Typeface` y las conserva en el almacenamiento privado de la aplicación.
Las hojas que solo incluyen WOFF/WOFF2 necesitan una versión TTF/OTF. Una ruta
local debe apuntar a un archivo que la aplicación pueda leer. El error se muestra
en el panel y el estilo anterior se conserva cuando falla la importación.

Desde 1.2.2, el importador conserva cada peso normal declarado en el CSS.
GothamPro usa sus archivos auténticos Light (300), Regular (400), Medium (500),
Bold (700) y Black (900), en lugar de intentar engrosar únicamente Regular.
Las importaciones antiguas se actualizan en segundo plano al usar esa familia,
con conexión a Internet. Si estás sin conexión, se conserva la fuente anterior;
también puedes volver a pulsar **Cargar GothamPro de Noir** para reimportarla.

Los subtítulos incrustados en los píxeles del vídeo no pueden personalizarse.
Los cues de imagen de media3 conservan su dibujo nativo. Crunchyroll conserva el
script y los handles de libass y usa su reloj de renderizado para el texto
personalizado. Al igual que el overlay de Noir, el texto ASS se presenta como
texto básico: no conserva karaoke, dibujos ni posicionamiento individual de
carteles. Desactiva **Usar estilo personalizado** para recuperar el ASS nativo.

## Validación realizada y pendiente

El paquete compila en GitHub Actions y las aplicaciones de ensayo se parchean
con Morphe Desktop 1.18.0. En un emulador Android 37 se comprobaron los hooks DEX,
los cambios sobre un subtítulo pausado, el efecto visual de las 15 propiedades,
su persistencia, los cues superpuestos, los saltos de línea, los saltos hacia
atrás, la expiración, la liberación de pistas y la vuelta al dibujo nativo.
El panel se inspeccionó en vertical y horizontal.
También pasaron la importación de GothamPro desde el CSS de Noir y su caché local.

Capturas de la aplicación de ensayo:
[vertical](screenshots/subtitles-portrait.png) y
[horizontal](screenshots/subtitles-landscape.png).

En 1.2.1 se reprodujo el fallo de 1.2.0 con el APKM original de Crunchyroll
3.117.0 (1175). Morphe Desktop aplicó los tres parches y generó el APK completo.
Una prueba de instrumentación dentro de ese APK comprobó el JNI de libass,
la carga de la pista, el reloj del controlador y el dibujo de `SubtitlesView`,
el cambio de color con el subtítulo pausado, la expiración, la restauración del
ASS nativo y la limpieza al destruir la pista. Se ejecutó en Android 37.

`renderFrame(JJ)` es nativo: el parche conserva su nombre, firma e implementación
JNI y captura sus argumentos en las llamadas de interfaz y de clase.
La prueba de regresión usa también un método JNI real con ambas formas de llamada.

No se comprobó una sesión de streaming con una cuenta ni el dispositivo Xiaomi.
Los APK comerciales de las otras tres aplicaciones siguen pendientes de validar.

## Repetir las pruebas

Usa un emulador aislado: los APK de ensayo usan los nombres de paquete de
Crunchyroll y HBO Max. Necesitas JDK 21 o posterior, Android SDK con plataforma
35, herramientas 35.0.1 y NDK 27.0.12077973, `adb`, el `.mpp` y Morphe Desktop 1.18.0.

```powershell
python tests/run_fixtures.py --sdk C:/Users/tu_usuario/AppData/Local/Android/Sdk --bundle patches-1.2.1.mpp --morphe morphe-desktop-1.18.0-all.jar --out C:/Temp/aimal-fixtures --fonts
```

Los resultados quedan en `fixture-result.txt`, `font-result.txt` y
`patch-result.json` dentro de cada carpeta de ensayo. Los APK de ensayo no se
distribuyen como versiones de las aplicaciones comerciales.

Para comprobar el renderer comercial de Crunchyroll, usa tu propio APKM:

```powershell
java -jar morphe-desktop-1.18.0-all.jar patch -p patches-1.2.1.mpp --exclusive -e "Subtitle styling" -e "Playback speed" -e "Aspect ratio control" Crunchyroll-3.117.0.apkm -o patched.apk --unsigned
python tests/run_vendor_crunchyroll.py --sdk C:/Users/tu_usuario/AppData/Local/Android/Sdk --apk patched.apk --out C:/Temp/aimal-vendor
```

Esta prueba requiere exactamente un emulador aislado conectado. Instala el APK
con una firma temporal bajo el paquete de Crunchyroll; elimina primero cualquier
aplicación de ensayo con ese paquete. No inicia sesión ni reproduce contenido
remoto. El APK comercial no se incluye en el repositorio ni en los releases.
