import React, {useState} from 'react';
import {
  Image,
  Text,
  View,
  StyleSheet,
  TextInput,
  Button,
  ActivityIndicator,
} from 'react-native';
import {createThumbnail} from 'react-native-create-thumbnail';

const placeholderImage = require('./assets/placeholder-image.png');

export default function App() {
  const [path, setPath] = useState('');
  const [thumbnail, setThumbnail] = useState('');
  const [timeStamp, setTimeStamp] = useState('1000');
  const [isLoading, setIsLoading] = useState(false);

  return (
    <View style={styles.container}>
      <Text style={styles.welcome}>☆CreateThumbnail example☆</Text>
      <TextInput
        value={path}
        onChangeText={setPath}
        style={styles.pathInput}
        placeholder="Paste video url"
        placeholderTextColor="lightgrey"
      />
      <TextInput
        keyboardType="numeric"
        value={timeStamp}
        onChangeText={setTimeStamp}
        style={styles.timeInput}
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
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 20,
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
  pathInput: {
    backgroundColor: '#eaeaea',
    width: '80%',
    paddingHorizontal: 10,
    color: 'black',
    borderColor: 'lightgrey',
    borderWidth: 1,
  },
  timeInput: {
    backgroundColor: '#eaeaea',
    width: '40%',
    paddingHorizontal: 10,
    margin: 20,
    color: 'black',
    borderColor: 'lightgrey',
    borderWidth: 1,
  },
});
