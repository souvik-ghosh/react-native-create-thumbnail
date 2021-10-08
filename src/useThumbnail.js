import { useEffect, useRef, useState } from "react";
import { createThumbnail } from "./createThumbnail";

export default function useThumbnail(videoUrl, options) {
  const [thumbnail, setThumbnail] = useState("");
  const mounted = useRef(false);

  useEffect(() => {
    mounted.current = true;
    return () => {
      mounted.current = false;
    };
  }, []);

  useEffect(() => {
    if (videoUrl) {
      createThumbnail({
        url: videoUrl,
        ...options,
      })
        .then((response) => {
          if (mounted.current) {
            setThumbnail(response.path);
          }
        })
        .catch((err) => console.log('createThumbnail', { err }));
    }
  }, [videoUrl]);

  return thumbnail;
}
