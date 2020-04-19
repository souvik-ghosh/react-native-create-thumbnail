import React, { Component } from "react";
import { Image, Text, View, StyleSheet } from "react-native";
import { createThumbnail } from "react-native-create-thumbnail";

export default class App extends Component {
  state = {
    status: "starting",
    thumbnail: null
  };

  componentDidMount() {
    createThumbnail({
      url: 'https://www.example.com/video-file.mp4',
      timeStamp: 10000
    })
      .then(response => {
        console.log({ response });
        this.setState({
          status: "Thumbnail received",
          thumbnail: response.path
        });
      })
      .catch(err => console.log({ err }));
  }

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>☆CreateThumbnail example☆</Text>
        <Text style={styles.instructions}>STATUS: {this.state.status}</Text>
        <Text style={styles.welcome}>☆THUMBNAIL☆</Text>
        {this.state.thumbnail && (
          <Image style={styles.image} source={{ uri: this.state.thumbnail }} />
        )}
      </View>
    );
  }
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
  image: { height: 150, width: 250, backgroundColor: "lightgrey" }
});
