package net.schwarzbaer.system;

import java.awt.Color;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Settings<ValueGroup extends Enum<ValueGroup> & Settings.GroupKeys<ValueKey>, ValueKey extends Enum<ValueKey>> {

	interface GroupKeys<V> {
		V[] getKeys();
	}
	
	private Preferences preferences;
	
	public Settings(Class<?> classObj) {
		this.preferences = Preferences.userNodeForPackage(classObj);
	}

	public boolean isSet(ValueGroup valueGroup) {
		return contains(valueGroup.getKeys());
	}
	
	protected boolean getBool  (ValueKey key               ) { return preferences.getBoolean(key.toString(), true ); }
	protected boolean getBool  (ValueKey key, boolean def  ) { return preferences.getBoolean(key.toString(), def  ); }
	protected void    putBool  (ValueKey key, boolean value) {        preferences.putBoolean(key.toString(), value); }
	protected float   getFloat (ValueKey key               ) { return preferences.getFloat  (key.toString(), 0    ); }
	protected float   getFloat (ValueKey key, float def    ) { return preferences.getFloat  (key.toString(), def  ); }
	protected void    putFloat (ValueKey key, float value  ) {        preferences.putFloat  (key.toString(), value); }
	protected double  getDouble(ValueKey key               ) { return preferences.getDouble (key.toString(), 0    ); }
	protected double  getDouble(ValueKey key, double def   ) { return preferences.getDouble (key.toString(), def  ); }
	protected void    putDouble(ValueKey key, double value ) {        preferences.putDouble (key.toString(), value); }
	protected int     getInt   (ValueKey key               ) { return preferences.getInt    (key.toString(), 0    ); }
	protected int     getInt   (ValueKey key, int def      ) { return preferences.getInt    (key.toString(), def  ); }
	protected void    putInt   (ValueKey key, int value    ) {        preferences.putInt    (key.toString(), value); }
	protected String  getString(ValueKey key               ) { return preferences.get       (key.toString(), null ); }
	protected String  getString(ValueKey key, String def   ) { return preferences.get       (key.toString(), def  ); }
	protected void    putString(ValueKey key, String value ) {        preferences.put       (key.toString(), value); }

	protected Color   getColor (ValueKey key              ) { return new Color(getInt(key, Color.BLACK.getRGB()), true); }
	protected Color   getColor (ValueKey key, Color def   ) { return new Color(getInt(key,         def.getRGB()), true); }
	protected void    putColor (ValueKey key, Color value ) { putInt(key, value.getRGB()); }


	private boolean contains(String[] prefkeys, ValueKey key) {
		if (key==null) return true;
		if (prefkeys==null) return false;
		for (String prefkey:prefkeys)
			if (prefkey.equals(key.toString()))
				return true;
		return false;
	}

	protected boolean contains(ValueKey key1                                             ) { return contains(key1, null, null, null, null); }
	protected boolean contains(ValueKey key1, ValueKey key2                              ) { return contains(key1, key2, null, null, null); }
	protected boolean contains(ValueKey key1, ValueKey key2, ValueKey key3               ) { return contains(key1, key2, key3, null, null); }
	protected boolean contains(ValueKey key1, ValueKey key2, ValueKey key3, ValueKey key4) { return contains(key1, key2, key3, key4, null); }
	protected boolean contains(ValueKey key1, ValueKey key2, ValueKey key3, ValueKey key4, ValueKey key5) {
		String[] prefkeys;
		try { prefkeys = preferences.keys(); }
		catch (BackingStoreException e) { e.printStackTrace(); return false; }
		if (!contains(prefkeys, key1)) return false;
		if (!contains(prefkeys, key2)) return false;
		if (!contains(prefkeys, key3)) return false;
		if (!contains(prefkeys, key4)) return false;
		if (!contains(prefkeys, key5)) return false;
		return true;
	}

	protected boolean contains(ValueKey[] keys) {
		String[] prefkeys;
		try { prefkeys = preferences.keys(); }
		catch (BackingStoreException e) { e.printStackTrace(); return false; }
		for (ValueKey key:keys)
			if (!contains(prefkeys, key))
				return false;
		return true;
	}
}
