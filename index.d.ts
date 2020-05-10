declare module "react-native-create-thumbnail" {
  export interface Config {
    url: string;
    timeStamp?: number;
    format?: "jpeg" | "png";
    dirSize?: number;
    headers?: object;
  }

  export interface Thumbnail {
    path: string;
    width: number;
    height: number;
  }

  export function createThumbnail(config: Config): Promise<Thumbnail>;

  export interface CreateThumbnail {
    createThumbnail(config: Config): Promise<Thumbnail>;
  }

  const CreateThumbnail: CreateThumbnail;

  export default CreateThumbnail;
}
