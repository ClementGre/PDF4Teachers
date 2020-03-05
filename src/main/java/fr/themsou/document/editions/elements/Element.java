package fr.themsou.document.editions.elements;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Element {

	void writeSimpleData(DataOutputStream writer) throws IOException;


	void select();
	void delete();

	int getPageNumber();
	int getCurrentPageNumber();
	Element clone();


}
