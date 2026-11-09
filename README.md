# Detección de Distancia de Peatones con YOLO26 🚀

**Detección de Distancia de Peatones con YOLO26** es una aplicación móvil de visión por computadora para Android desarrollada para realizar **detección de personas y estimación de distancia en tiempo real** utilizando la cámara en vivo y modelos de inteligencia artificial como **Ultralytics YOLOv26n** y **TensorFlow Lite**.

---

## 👨‍💻 Creador y Desarrollador
* **Autor:** M.C. Emmanuelle Jasso
* **Proyecto:** Visión por Computadora & Detección de Distancia de Peatones en Android

---

## ✨ Características Principales

* 👤 **Detección Exclusiva de Peatones:** Identifica en tiempo real a personas en el flujo de la cámara.
* 📏 **Estimación Óptica de Distancia:** Calcula la distancia estimada en metros ($D = \frac{f_y \cdot H_{real}}{h_{bbox}}$) basándose en la estatura promedio de 1.65 m y la altura en píxeles del cuadro delimitador.
* ⚡ **Modo Acelerado de Inferencia (Switch):**
  * **YOLOv26n (Predeterminado):** Máxima precisión de detección de personas (~45 ms).
  * **MobileNet SSD V1 (Modo Alta Velocidad):** Máxima velocidad de inferencia (~11 ms).
* ⚙️ **Panel de Control Dinámico:** Umbral de confianza, resultados máximos, hilos de ejecución y aceleración por hardware (CPU/GPU/NNAPI).
* 🎨 **Diseño Limpio y Moderno:** Interfaz estilizada *Dark Glassmorphism* sin distractores visuales.

---

## 📊 Comparativa de Modelos

| Modelo | Tiempo de Inferencia | Precisión | Uso Recomendado |
| :--- | :--- | :--- | :--- |
| **YOLOv26n** (Predeterminado) | ~45 ms promedio | 🎯 **Muy alta** | Análisis de precisión para detección constante de personas. |
| **MobileNet SSD V1** (Modo Alta Velocidad) | ⚡ ~11 ms promedio | 🚀 **Ultra rápido** | Mayor fluidez de FPS cuando la velocidad es la prioridad. |

---

## 🛠️ Requisitos del Sistema

* **Android Studio:** 2024.1+ (Ladybug / Meerkat / 2026+).
* **JDK:** Java 17.
* **SDK Android:** minSdk 26, targetSdk 35.

---

## 📄 Licencia

* **Desarrollado por:** M.C. Emmanuelle Jasso
