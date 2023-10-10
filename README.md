# react-native-create-thumbnail

iOS/Android thumbnail generator with storage/cache management and support for both local and remote videos. `react-native-create-thumbnail` is a wrapper around
[`AVAssetImageGenerator`](https://developer.apple.com/documentation/avfoundation/avassetimagegenerator?language=objc) (iOS) and [`MediaMetadataRetriever`](https://developer.android.com/reference/android/media/MediaMetadataRetriever) (Android)

[![npm version](https://img.shields.io/npm/v/react-native-create-thumbnail.svg)](https://npmjs.com/package/react-native-create-thumbnail) [![npm downloads](https://img.shields.io/npm/dm/react-native-create-thumbnail.svg)](https://npmjs.com/package/react-native-create-thumbnail) [![Maintenance Status](https://img.shields.io/badge/maintenance-active-green.svg)](#maintenance-status)

## Getting started

1. Install library from `npm`

   ```bash
   npm i react-native-create-thumbnail
   ```

   or

   ```bash
   yarn add react-native-create-thumbnail
   ```

2. Link native code

   With autolinking (react-native 0.60+)

   ```bash
   cd ios && pod install
   ```

   Pre 0.60

   ```bash
   react-native link react-native-create-thumbnail
   ```

## Usage

```javascript
import { createThumbnail } from "react-native-create-thumbnail";

createThumbnail({
  url: '<path to video file>',
  timeStamp: 10000,
})
  .then(response => console.log({ response }))
  .catch(err => console.log({ err }));
```

## Request Object

| Property  |           Type            | Description                                                               |
| --------- | :-----------------------: | :------------------------------------------------------------------------ |
| url       |    `String` (required)    | Path to video file (local or remote)                                      |
| timeStamp |  `Number` (default `0`)   | Thumbnail timestamp (in milliseconds)                                     |
| format    | `String` (default `jpeg`) | Thumbnail format, can be one of: `jpeg`, or `png`                         |
| dirSize   | `Number` (default `100`)  | Maximum size of the cache directory (in megabytes). When this directory is full, the previously generated thumbnails will be deleted to clear about half of it's size.                        |
| headers   |         `Object`          | Headers to load the video with. e.g. `{ Authorization: 'someAuthToken' }` |
| cacheName   |         `String` (optional)          | Cache name for this thumbnail to avoid duplicate generation. If specified, and a thumbnail already exists with the same cache name, it will be returned instead of generating a new one. |

## Response Object

| Property |   Type   | Description                 |
| -------- | :------: | :-------------------------- |
| path     | `String` | Path to generated thumbnail |
| size     | `Number` | Size (in bytes) of thumbnail|
| mime     | `String` | Mimetype of thumbnail       |
| width    | `Number` | Thumbnail width             |
| height   | `Number` | Thumbnail height            |

#### Notes

Requires following Permissions on android

```bash
READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE
```

#### Limitations

- Remote videos aren't supported on android sdk_version < 14.
- This is a **Native Module**, so it won't work in expo managed projects.
- This library heavily depends on the native API's to generate the thumbnails. Thus it can only generate from the video formats/codecs that are supported by the device's OS.

#### Credits

- [`react-native-thumbnail`](https://www.npmjs.com/package/react-native-thumbnail) - A great source of inspiration
- This project was bootstrapped with [`create-react-native-module`](https://github.com/brodybits/create-react-native-module)

#### Maintenance Status

**Active:** Bug reports, feature requests and pull requests are welcome.
