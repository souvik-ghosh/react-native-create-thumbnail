import { useState } from 'react';
import {
  Image,
  Text,
  View,
  StyleSheet,
  Button,
  ActivityIndicator,
} from 'react-native';
import { createThumbnail } from 'react-native-create-thumbnail';
import Input from './Input';

const placeholderImage = require('./assets/placeholder-image.png');

const sampleVideoUrl = 'https://videos.pexels.com/video-files/4763824/4763824-uhd_2560_1440_24fps.mp4';

export default function App() {
  const [path, setPath] = useState(sampleVideoUrl);
  const [thumbnail, setThumbnail] = useState('');
  const [timeStamp, setTimeStamp] = useState('1000');
  const [isLoading, setIsLoading] = useState(false);

  return (
    <View style={styles.container}>
      <Text style={styles.welcome}>☆CreateThumbnail example☆</Text>
      <Input
        label="Video URL"
        value={path}
        onChangeText={setPath}
        placeholder="Paste video url"
      />
      <Input
        label="Timestamp"
        keyboardType="numeric"
        value={timeStamp}
        onChangeText={setTimeStamp}
        placeholder="Timestamp"
      />
      <Button
        title="Generate Thumbnail"
        disabled={isLoading}
        onPress={generateThumbnail}
      />
      <Text style={styles.welcome}>☆THUMBNAIL☆</Text>
      <View style={styles.image}>
        {isLoading ? (
          <ActivityIndicator size="large" />
        ) : (
          <Image
            style={styles.image}
            source={thumbnail ? {uri: thumbnail} : placeholderImage}
          />
        )}
      </View>
    </View>
  );

  async function generateThumbnail() {
    if (!path) {
      return;
    }

    setIsLoading(true);

    try {
      const response = await createThumbnail({
        url: path,
        timeStamp: parseInt(timeStamp, 10),
      });
      setThumbnail(response.path);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    backgroundColor: '#f5f6fa',
    padding: 20,
    paddingTop: 50,
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 30,
    color: 'black',
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 20,
  },
  image: {
    height: 150,
    width: 250,
    backgroundColor: 'lightgrey',
    justifyContent: 'center',
  },
});
