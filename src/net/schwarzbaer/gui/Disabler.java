package net.schwarzbaer.gui;

import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;

public class Disabler<ActionCommands> {
	
	private HashMap<ActionCommands, Vector<JComponent>> map;

	public Disabler() {
		map = new HashMap<ActionCommands,Vector<JComponent>>();
	}
	
	public void setCareFor(ActionCommands actionCommand) {
		map.put(actionCommand,new Vector<JComponent>());
	}

	public boolean caresFor(ActionCommands actionCommand) {
		return map.containsKey(actionCommand);
	}

	public void showMap() {
		System.out.println("Disabler.map:");
		for (ActionCommands key:map.keySet()) {
			System.out.printf("   key[%s]\r\n",key);
			Vector<JComponent> list = map.get(key);
			if (list==null)
				System.out.printf("      <no entries>\r\n");
			else
				for (JComponent comp:list)
					System.out.printf("      %s\r\n",comp);
		}
	}

	public JComponent add(ActionCommands actionCommand, JComponent comp) {
		Vector<JComponent> list = map.get(actionCommand);
		if (list==null) throw new UnsupportedOperationException("Disabler: Can't add components for unregistered ActionCommand "+actionCommand+". Please register it with disabler.setCareFor.");
		list.add(comp);
		return comp;
	}

	public void setEnableAll(boolean enabled) {
		for (ActionCommands key:map.keySet())
			setEnable(key, enabled);
	}

	public void setEnableAll( boolean enabled, ActionCommands exceptThis ) {
		for (ActionCommands key:map.keySet())
			if (!exceptThis.equals(key))
				setEnable(key, enabled);
	}

	public void setEnableAll( boolean enabled, Vector<ActionCommands> exceptThis ) {
		for (ActionCommands key:map.keySet())
			if (!exceptThis.contains(key))
				setEnable(key, enabled);
	}

	public void setEnable(ActionCommands actionCommand, boolean enabled) {
		Vector<JComponent> list = map.get(actionCommand);
		if (list==null) throw new UnsupportedOperationException("Disabler: Can't use method setEnable on ActionCommand "+actionCommand+".");
		for (JComponent c:list)
			c.setEnabled(enabled);
	}

	public Set<ActionCommands> keySet() {
		return map.keySet();
	}
	
	public Vector<JComponent> get(ActionCommands key) {
		return map.get(key);
	}
}
