
# 🚀 Meme Swiper - JavaFX Desktop App

Bienvenido a **Meme Swiper**, una aplicación de escritorio moderna construida con **JavaFX** y **Gradle** que permite explorar, guardar y gestionar tus memes favoritos de forma fluida y visual.
**NOTA**: La aplicacion continua en desarrollo aunque ya se puede acceder a las funcionalidades principales
---

## 📋 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

* **Java JDK 25 o superior**: El proyecto se ha elaborado en esta version
* **Conexión a Internet**: Necesaria para descargar dependencias y obtener los memes en tiempo real.

---

## 🛠️ Instrucciones de Instalación

1. **Clonar el repositorio:**
```bash
git clone https://github.com/TU_USUARIO/NOMBRE_DEL_REPOSITORIO.git
cd NOMBRE_DEL_REPOSITORIO

```


2. **Ejecutar la aplicación:**
Usa el wrapper de Gradle incluido (no necesitas instalar Gradle globalmente):
* **Windows:** `./gradlew run`
* **Linux/macOS:** `chmod +x gradlew && ./gradlew run`

---

## 🕹️ Funcionalidades Principales

* **Visor con Swipe:** Sistema de cartas para navegar por memes aleatorios.
* **Galería de Favoritos:** Guarda tus memes preferidos en un archivo local `liked_memes.json`.
* **Vista de Detalle:** Inspecciona memes en alta resolución, consulta metadatos y descárgalos directamente a tu PC.
* **Gestión de Memoria:** Carga asíncrona de imágenes para una experiencia sin interrupciones.

---

## 🌍 Créditos de la API

Este proyecto utiliza la excelente API de memes desarrollada por **D3vd**. Gracias a este servicio, podemos obtener contenido actualizado de los subreddits más populares de forma aleatoria.

* **API utilizada:** [Meme_Api](https://github.com/D3vd/Meme_Api)
* **Autor:** [@D3vd](https://github.com/D3vd)

---

## 📂 Estructura del Proyecto

* `src/main/java`: Lógica de controladores (`HelloController`, `GalleryController`, `DetailController`) y modelos de datos.
* `src/main/resources`: Archivos **FXML** para la arquitectura de la UI y **CSS** para el diseño oscuro personalizado.
* `build.gradle`: Configuración de módulos de JavaFX y dependencias externas.

---

## 🛠️ Tecnologías Utilizadas

* **Java 25** & **JavaFX 21**
* **Gradle** (Build Tool)
* **Jackson** (Serialización JSON)
* **Meme_Api** (Fuente de datos externa)

---
