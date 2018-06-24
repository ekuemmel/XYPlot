package de.ewmksoft.example;

import java.util.NoSuchElementException;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
	private IDataStorage dataStorage1;
	private IDataStorage dataStorage2;
	private IDataStorage dataStorage3;

	IDataStorage getDataStorage(int num, Context context) {
		switch (num) {
		case 1:
			if (dataStorage1 == null) {
				dataStorage1 = new DataStorage1();
			}
			return dataStorage1;
		case 2:
			if (dataStorage2 == null) {
				dataStorage2 = new DataStorage2();
			}
			return dataStorage2;
		case 3:
			if (dataStorage3 == null) {
				dataStorage3 = new DataStorage3(context);
			}
			return dataStorage3;
		default:
			throw new NoSuchElementException();
		}

	}
}
