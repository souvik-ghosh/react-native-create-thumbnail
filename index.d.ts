declare module "react-native-create-thumbnail" {
  export interface Config {
    url: string;
    timeStamp?: number;
    format?: "jpeg" | "png";
    dirSize?: number;
    headers?: object;
    cacheName?: string;
  }

  export interface Thumbnail {
    path: string;
    size: number;
    mime: string;
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
