# PhotoMagic: Powerful Photo Editor Library for Android

PhotoMagic is a versatile and easy-to-use photo editor library for Android applications. It provides a wide range of features to enhance your photos with customizable effects, filters, text overlays, and more. Whether you're building a social media app, a camera app, or any image-centric application, PhotoMagic simplifies photo editing with its powerful capabilities.

## 🎉 Key Features

- **Filters & Effects**: Apply stunning filters and effects to transform your photos.
- **Text Overlay**: Add text with customizable fonts, colors, and styles.
- **Cropping**: Easily crop the photo supported frames: Free, Square, 3:4, 4:3.
- **Rotating**: Simple rotate the photo with Left, Right, Mirror, Flip.
- **Perspective**: Feel free change your view of the photo with Horizotal and Vertical.

## 📦 Dependency

Add the following dependency to your `build.gradle` file:

```gradle
dependencies {
    implementation 'com.github.stdlam.photomagic:photoeditor:1.0.1'
}
```

## 🚀 Getting Started
To start using the PhotoMagic library, simply initialize it in your activity or fragment:
```
PhotoEditProvider(context).startEdit(selectedImageUri)

// Compose:
PhotoEditProvider(LocalContext.current).startEdit(selectedImageUri)
```

## 🛠️ Requirements
- Android API Level: 24 and above
- Language: Kotlin / Java

## 🤝 Contributions
We welcome contributions! Feel free to submit a pull request or open an issue for any feature requests or bug reports.
