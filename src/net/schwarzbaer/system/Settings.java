package net.schwarzbaer.system;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
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
	
	public boolean getBool  (ValueKey key               ) { return preferences.getBoolean(key.toString(), true ); }
	public boolean getBool  (ValueKey key, boolean def  ) { return preferences.getBoolean(key.toString(), def  ); }
	public void    putBool  (ValueKey key, boolean value) {        preferences.putBoolean(key.toString(), value); }
	public float   getFloat (ValueKey key               ) { return preferences.getFloat  (key.toString(), 0    ); }
	public float   getFloat (ValueKey key, float def    ) { return preferences.getFloat  (key.toString(), def  ); }
	public void    putFloat (ValueKey key, float value  ) {        preferences.putFloat  (key.toString(), value); }
	public double  getDouble(ValueKey key               ) { return preferences.getDouble (key.toString(), 0    ); }
	public double  getDouble(ValueKey key, double def   ) { return preferences.getDouble (key.toString(), def  ); }
	public void    putDouble(ValueKey key, double value ) {        preferences.putDouble (key.toString(), value); }
	public int     getInt   (ValueKey key               ) { return preferences.getInt    (key.toString(), 0    ); }
	public int     getInt   (ValueKey key, int def      ) { return preferences.getInt    (key.toString(), def  ); }
	public void    putInt   (ValueKey key, int value    ) {        preferences.putInt    (key.toString(), value); }
	public String  getString(ValueKey key               ) { return preferences.get       (key.toString(), null ); }
	public String  getString(ValueKey key, String def   ) { return preferences.get       (key.toString(), def  ); }
	public void    putString(ValueKey key, String value ) {        preferences.put       (key.toString(), value); }

	public Color   getColor (ValueKey key              ) { return new Color(getInt(key, Color.BLACK.getRGB()), true); }
	public Color   getColor (ValueKey key, Color def   ) { return new Color(getInt(key,         def.getRGB()), true); }
	public void    putColor (ValueKey key, Color value ) { putInt(key, value.getRGB()); }

	public Dimension getDimension(ValueKey keyW, ValueKey keyH, int defW, int defH) { int w=getInt(keyW,defW); int h=getInt(keyH,defH); return new Dimension(w,h); }
	public Dimension getDimension(ValueKey keyW, ValueKey keyH                    ) { int w=getInt(keyW     ); int h=getInt(keyH     ); return new Dimension(w,h); }
	public void      putDimension(ValueKey keyW, ValueKey keyH, Dimension size    ) { putInt(keyW, size.width ); putInt(keyH, size.height); }
	
	public Point getPoint(ValueKey keyX, ValueKey keyY                ) { int x=getInt(keyX); int y=getInt(keyY); return new Point(x,y); }
	public void  putPoint(ValueKey keyX, ValueKey keyY, Point location) { putInt(keyX, location.x); putInt(keyY, location.y); }


	private boolean contains(String[] prefkeys, ValueKey key) {
		if (key==null) return true;
		if (prefkeys==null) return false;
		for (String prefkey:prefkeys)
			if (prefkey.equals(key.toString()))
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
}
