package net.schwarzbaer.system;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Settings<ValueGroup extends Enum<ValueGroup> & Settings.GroupKeys<ValueKey>, ValueKey extends Enum<ValueKey>> {

	public interface GroupKeys<V> {
		V[] getKeys();
	}
	
	private Preferences preferences;
	
	public Settings(Class<?> classObj) {
		this.preferences = Preferences.userNodeForPackage(classObj);
	}

	public boolean isSet(ValueGroup valueGroup) {
		return contains(valueGroup.getKeys());
	}
	
	public void    remove   (ValueKey key               ) {        preferences.remove    (key.name()       ); }
	
	public boolean getBool  (ValueKey key               ) { return preferences.getBoolean(key.name(), true ); }
	public boolean getBool  (ValueKey key, boolean def  ) { return preferences.getBoolean(key.name(), def  ); }
	public void    putBool  (ValueKey key, boolean value) {        preferences.putBoolean(key.name(), value); }
	public float   getFloat (ValueKey key               ) { return preferences.getFloat  (key.name(), 0    ); }
	public float   getFloat (ValueKey key, float def    ) { return preferences.getFloat  (key.name(), def  ); }
	public void    putFloat (ValueKey key, float value  ) {        preferences.putFloat  (key.name(), value); }
	public double  getDouble(ValueKey key               ) { return preferences.getDouble (key.name(), 0    ); }
	public double  getDouble(ValueKey key, double def   ) { return preferences.getDouble (key.name(), def  ); }
	public void    putDouble(ValueKey key, double value ) {        preferences.putDouble (key.name(), value); }
	public int     getInt   (ValueKey key               ) { return preferences.getInt    (key.name(), 0    ); }
	public int     getInt   (ValueKey key, int def      ) { return preferences.getInt    (key.name(), def  ); }
	public void    putInt   (ValueKey key, int value    ) {        preferences.putInt    (key.name(), value); }
	public String  getString(ValueKey key               ) { return preferences.get       (key.name(), null ); }
	public String  getString(ValueKey key, String def   ) { return preferences.get       (key.name(), def  ); }
	public void    putString(ValueKey key, String value ) { if (value==null) remove(key); else preferences.put(key.name(), value); }

	public Color   getColor (ValueKey key              ) { return new Color(getInt(key, Color.BLACK.getRGB()), true); }
	public Color   getColor (ValueKey key, Color def   ) { return new Color(getInt(key,         def.getRGB()), true); }
	public void    putColor (ValueKey key, Color value ) { putInt(key, value.getRGB()); }

	public File    getFile  (ValueKey key              ) { return getFile(key,null); }
	public File    getFile  (ValueKey key, File def    ) { String str = getString(key,null); if (str==null) return def ; return new File(str); }
	public void    putFile  (ValueKey key, File value  ) { putString(key, value.getAbsolutePath()); }

	public Dimension getDimension(ValueKey keyW, ValueKey keyH, int defW, int defH) { int w=getInt(keyW,defW); int h=getInt(keyH,defH); return new Dimension(w,h); }
	public Dimension getDimension(ValueKey keyW, ValueKey keyH                    ) { int w=getInt(keyW     ); int h=getInt(keyH     ); return new Dimension(w,h); }
	public void      putDimension(ValueKey keyW, ValueKey keyH, Dimension size    ) { putInt(keyW, size.width ); putInt(keyH, size.height); }
	
	public Point getPoint(ValueKey keyX, ValueKey keyY                ) { int x=getInt(keyX); int y=getInt(keyY); return new Point(x,y); }
	public void  putPoint(ValueKey keyX, ValueKey keyY, Point location) { putInt(keyX, location.x); putInt(keyY, location.y); }

	public <E extends Enum<E>> E    getEnum(ValueKey key       , Class<E> enumClass ) { return convert(getString(key,null),null, enumClass); }
	public <E extends Enum<E>> E    getEnum(ValueKey key, E def, Class<E> enumClass ) { return convert(getString(key,null),def , enumClass); }
	public <E extends Enum<E>> void putEnum(ValueKey key, E value ) { putString(key, value==null ? null : value.name()); }
	
	private <E extends Enum<E>> E convert(String string, E def, Class<E> enumClass) {
		if (string==null) return def;
		try { return Enum.valueOf(enumClass, string); }
		catch (Exception e) { return def; }
	}

	private boolean contains(String[] prefkeys, ValueKey key) {
		if (key==null) return true;
		if (prefkeys==null) return false;
		for (String prefkey:prefkeys)
			if (prefkey.equals(key.name()))
				return true;
		return false;
	}

	public boolean contains(ValueKey key1                                             ) { return contains(key1, null, null, null, null); }
	public boolean contains(ValueKey key1, ValueKey key2                              ) { return contains(key1, key2, null, null, null); }
	public boolean contains(ValueKey key1, ValueKey key2, ValueKey key3               ) { return contains(key1, key2, key3, null, null); }
	public boolean contains(ValueKey key1, ValueKey key2, ValueKey key3, ValueKey key4) { return contains(key1, key2, key3, key4, null); }
	public boolean contains(ValueKey key1, ValueKey key2, ValueKey key3, ValueKey key4, ValueKey key5) {
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

	public boolean contains(ValueKey[] keys) {
		String[] prefkeys;
		try { prefkeys = preferences.keys(); }
		catch (BackingStoreException e) { e.printStackTrace(); return false; }
		for (ValueKey key:keys)
			if (!contains(prefkeys, key))
				return false;
		return true;
	}
	
	public static class Global extends Settings<Global.ValueGroup,Global.ValueKey> {
		
		private static Global instance = null;
		public static Global getInstance() {
			if (instance==null)
				instance = new Global();
			return instance;
		}

		public enum ValueGroup implements Settings.GroupKeys<ValueKey> {
			;
			ValueKey[] keys;
			ValueGroup(ValueKey...keys) { this.keys = keys;}
			@Override public ValueKey[] getKeys() { return keys; }
		}

		public enum ValueKey {
			VrmlViewer
		}

		public Global() {
			super(Global.class);
		}
	}
}
