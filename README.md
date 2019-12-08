# react-native-create-thumbnail

iOS/Android thumbnail generator with support for both local and remote videos

## Getting started

1. Install library from `npm`

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
  url: "path to video file",
  timeStamp: 10
})
  .then(response => {
    console.log({ response });
    this.setState({
      status: "Thumbnail received",
      thumbnail: response.path
    });
  })
  .catch(err => console.log({ err }));
```

## Request Object

| Property  |          Type           | Description                           |
| --------- | :---------------------: | :------------------------------------ |
| url       | `String` (default `""`) | Path to video file (local or remote). |
| timeStamp | `Number` (default `1`)  | Thumbnail time                        |

## Response Object

| Property |   Type   | Description                  |
| -------- | :------: | :--------------------------- |
| path     | `String` | Path to generated thumbnail. |
| width    | `Number` | Thumbnail width              |
| height   | `Number` | Thumbnail height             |

## License

_MIT_
