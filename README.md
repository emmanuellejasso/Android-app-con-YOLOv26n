# Android App con YOLO26 🚀

**Android App con YOLO26** es una aplicación móvil avanzada de visión por computadora para Android que realiza **detección de peatones (personas)**, **estimación de pose corporal en tiempo real con MediaPipe** y **cálculo de distancia óptica en metros** basado en la estatura promedio del usuario (1.65 m).

---

## 👨‍💻 Creador y Desarrollador
* **Autor:** M.C. Emmanuelle Jasso
* **Proyecto:** Detección de Peatones, Estimación de Pose y Distancia Óptica en Android

---

## ✨ Características Principales

* ⚡ **Detección de Peatones en Tiempo Real:** Procesamiento de flujo de cámara a 60 FPS utilizando **CameraX** y **YOLOv26n**.
* 🚶 **Detección Exclusiva de Personas:** Filtrado estricto de la clase `Persona` / `Person`.
* 📏 **Estimación Óptica de Distancia:** Cálculo en metros basado en la estatura promedio (1.65 m en México) y la altura normalizada de la caja delimitadora en pantalla.
* 🧘 **Estimación de Pose con MediaPipe:** Proyección en tiempo real de los 33 puntos clave anatómicos (cabeza, hombros, codos, manos, cadera, rodillas y pies) mapeados dentro de la caja azul del peatón.
* 🚀 **Modo Alta Velocidad (Switch):** Conmutación rápida entre **YOLOv26n** (Precisión y Rendimiento) y **MobileNet V1** (Alta Velocidad).
* ⚙️ **Panel de Control:** Ajuste fino de umbral de confianza, hilos de ejecución y estimación de pose.

---

## 🛠️ Requisitos del Sistema

* **Android Studio:** 2024.1+ (Ladybug / Meerkat / 2026+).
* **JDK:** Java 17.
* **SDK Android:** minSdk 26, targetSdk 35.

---

## 📄 Licencia

* **Desarrollado por:** M.C. Emmanuelle Jasso
