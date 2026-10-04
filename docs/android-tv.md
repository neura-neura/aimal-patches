# Crunchyroll Android TV 3.74.0

El parche **Subtitle styling (Android TV)** agrega las 15 opciones de Noir,
una vista previa y cambios inmediatos en los subtítulos de texto de media3.
Es específico de `com.crunchyroll.crunchyroid` **3.74.0 (22364)** para TV.
Los subtítulos de imagen conservan el dibujo original.

## Usar el mando

Durante un episodio, mantén **OK / Enter** al menos un segundo y suéltalo.
También puedes pulsar **Menú** si tu mando tiene ese botón. Se abre el editor,
sin una pastilla permanente sobre el vídeo. Una pulsación corta de OK conserva
la acción del reproductor; la acción se entrega al soltar el botón.

El editor empieza con foco en **Cerrar**. Usa las flechas para recorrer los
controles, **izquierda/derecha** para ajustar los deslizadores y **OK** para
activar botones y opciones. Los campos permiten valores exactos con el teclado
de la TV. **Atrás** cierra el editor; los cambios ya están guardados.
**FIT / STRETCH** cambia el ajuste de imagen. **Elegir o importar fuente →
Cargar GothamPro de Noir** descarga las variantes reales de Gotham; necesita
Internet. La primera carga puede tardar. El peso cambia al seleccionar su
archivo correspondiente, incluidas las variantes 500 y 900.

## Xiaomi Mi Box con Android 9

La instalación preparada conserva ARM de 32 y 64 bits; no necesitas elegir un
archivo por arquitectura. En Android 9, la autorización para instalar APKs se
concede al gestor de archivos que uses. Sigue la pantalla de autorización que
aparezca al abrir el APK.

## Instalar sin Morphe en la TV

1. Copia el APK firmado que se preparó en tu PC a una memoria USB y conéctala a
   la TV box. También puedes transferirlo por tu método habitual de archivos.
2. Si está instalada la versión oficial de Crunchyroll, desinstálala primero:
   tiene otra firma. Esto borra los datos locales y tendrás que iniciar sesión
   de nuevo. Conserva tus credenciales antes de hacerlo.
3. Abre el APK desde un gestor de archivos de la TV. Si Android lo solicita,
   permite instalar aplicaciones desconocidas para ese gestor. En versiones
   antiguas, la opción puede estar en **Seguridad → Fuentes desconocidas**.
4. Instálalo y abre Crunchyroll desde el launcher de TV. Inicia sesión y prueba
   un episodio con subtítulos. Mantén OK para abrir la personalización.

La TV box necesita Android 6.0 o posterior y ARM de 32 o 64 bits. El APK
combinado conserva ambas arquitecturas del archivo que compartiste.

También puedes instalar desde el PC con ADB si la TV ya tiene depuración
configurada y autorizada:

```powershell
adb -s SERIAL_DE_TU_TV install -r "C:\Users\neura\Downloads\Crunchyroll-TV-3.74.0-Aimal.apk"
```

## Volver a generar el APK

En Morphe Manager del móvil, actualiza la fuente
`https://github.com/neura-neura/aimal-patches`, selecciona el APKM original de
TV 3.74.0 y activa **únicamente Subtitle styling (Android TV)**. Exporta el APK
firmado y transfiérelo a la TV. Los parches del móvil (Subtitle styling,
Aspect ratio control y Playback speed) tienen otro reproductor.

El APK preparado en el PC se firma con una clave local, distinta de la que
Morphe Manager use en tu móvil. Para actualizar sin reinstalar, conserva el
mismo método y clave. La clave local se guarda en
`C:\Users\neura\.aimal-patches\signing`; no se publica en GitHub.
Los releases públicos contienen los parches `.mpp`, no el APK comercial.
