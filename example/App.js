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
      url:
        "https://uc8d882487c3bc5496dba3acd6ea.dl.dropboxusercontent.com/cd/0/get/At30lKs23Exff289oyuORmbjN1WgY0gVvQplB1PUw3Z9AMej2kWdCAR3MAq0BJ8krvYVhp0PjEEh0abLvi3qjUnZW8PxyM-Y7KiA9WaihEeVT7TPCZ400ZbNBofzyaL46T8/file?dl=1",
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
