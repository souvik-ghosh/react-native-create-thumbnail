import React, { useState } from "react";
import { Image, Text, View, StyleSheet, TextInput, Button } from "react-native";
import { createThumbnail } from "react-native-create-thumbnail";

export default function App() {
  const [path, setPath] = useState('');
  const [thumbnail, setThumbnail] = useState('');
  const [timeStamp, setTimeStamp] = useState('');

  const generate = () => {
    if (!path) {
      return;
    }

    createThumbnail({
      url: path,
      timeStamp: parseInt(timeStamp)
    })
      .then(response => {
        setThumbnail(response.path);
      })
      .catch(err => console.log({ err }));
  };

  return (
    <View style={styles.container}>
      <Text style={styles.welcome}>☆CreateThumbnail example☆</Text>
      <TextInput
        value={path}
        onChangeText={setPath}
        style={styles.pathInput}
        placeholder="Paste video url"
      />
      <TextInput
        value={timeStamp}
        onChangeText={setTimeStamp}
        style={styles.timeInput}
        placeholder="Timestamp"
      />
      <Button
        title="Generate Thumbnail"
        onPress={generate}
      />
      <Text style={styles.welcome}>☆THUMBNAIL☆</Text>
      {!!thumbnail && (
        <Image style={styles.image} source={{ uri: thumbnail }} />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#F5FCFF"
  },
  welcome: {
    fontSize: 20,
    textAlign: "center",
    margin: 10
  },
  instructions: {
    textAlign: "center",
    color: "#333333",
    marginBottom: 5
  },
  image: {
    height: 150,
    width: 250,
    backgroundColor: "lightgrey"
  },
  pathInput: {
    height: 35,
    backgroundColor: '#eaeaea',
    width: '80%',
    paddingHorizontal: 10
  },
  timeInput: {
    height: 35,
    backgroundColor: '#eaeaea',
    width: '40%',
    paddingHorizontal: 10,
    marginTop: 10
  }
});
