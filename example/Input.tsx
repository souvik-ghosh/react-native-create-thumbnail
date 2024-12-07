import React from 'react';
import { View, Text, TextInput, StyleSheet } from 'react-native';

interface InputProps extends React.ComponentPropsWithoutRef<typeof TextInput> {
  label: string;
  value: string;
  placeholder?: string;
  onChangeText?: (text: string) => void;
}

const Input: React.FC<InputProps> = ({ label, value, placeholder, onChangeText }) => {
  return (
    <View style={styles.container}>
      <Text style={styles.label}>{label}</Text>
      <View style={styles.inputContainer}>
        <TextInput
          style={styles.input}
          value={value}
          placeholder={placeholder}
          keyboardType="numeric"
          onChangeText={onChangeText}
        />
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginBottom: 20,
  },
  label: {
    fontSize: 16,
    color: '#7A7A7A',
    marginBottom: 5,
  },
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#ffffff',
    borderRadius: 10,
    paddingHorizontal: 10,
    paddingVertical: 8,
    width: '100%',
    height: 70,
  },
  input: {
    flex: 1,
    fontSize: 20,
    fontWeight: 'bold',
    color: '#2E2E2E',
  },
  currency: {
    fontSize: 16,
    color: '#7A7A7A',
    marginLeft: 8,
  },
});

export default Input;
