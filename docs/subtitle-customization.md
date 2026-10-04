# Personalización de subtítulos en Aimal

Este fork agrega un apartado **SUBTÍTULOS** al reproductor. Su vista previa y los
subtítulos durante la reproducción usan el mismo renderizador. Los cambios se
guardan al mover los controles; no hay un botón de aplicar ni hace falta cambiar
de episodio o de idioma. También se puede desactivar el estilo personalizado y
recuperar inmediatamente el dibujo original de la aplicación.

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

Las aplicaciones de ensayo reproducen las formas esperadas de las clases.
**No validan los fingerprints contra los APK comerciales ni una sesión de
streaming real.** La detección de la vista y el reloj ASS de Crunchyroll es nueva:
si una versión no coincide, el parche informa del error y detiene el parcheo.
La compatibilidad de este fork con las cuatro aplicaciones debe confirmarse
al probar los APK reales; las versiones anteriores de referencia vienen del
proyecto original.

## Repetir las pruebas

Usa un emulador aislado: los APK de ensayo usan los nombres de paquete de
Crunchyroll y HBO Max. Necesitas JDK 21 o posterior, Android SDK con plataforma
35 y herramientas 35.0.1, `adb`, el `.mpp` y Morphe Desktop 1.18.0.

```powershell
python tests/run_fixtures.py --sdk C:/Users/tu_usuario/AppData/Local/Android/Sdk --bundle patches-1.2.0.mpp --morphe morphe-desktop-1.18.0-all.jar --out C:/Temp/aimal-fixtures --fonts
```

Los resultados quedan en `fixture-result.txt`, `font-result.txt` y
`patch-result.json` dentro de cada carpeta de ensayo. Los APK de ensayo no se
distribuyen como versiones de las aplicaciones comerciales.
