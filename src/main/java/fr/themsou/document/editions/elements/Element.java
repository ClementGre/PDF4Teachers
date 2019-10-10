package fr.themsou.document.editions.elements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Element {

	void writeData(DataOutputStream writer) throws IOException;
	void delete();

	static Element readDataAndCreate(DataInputStream reader) throws IOException {
		return null;
	}
}
