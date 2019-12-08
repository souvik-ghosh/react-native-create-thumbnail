import { NativeModules } from "react-native";

const { CreateThumbnail } = NativeModules;

export const { create: createThumbnail } = CreateThumbnail;
export default CreateThumbnail;
