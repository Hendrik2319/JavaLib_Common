package net.schwarzbaer.gui;

import javax.swing.Icon;

public final class GeneralIcons
{
	public enum GrayCommandIcons {
		Muted, UnMuted, Up, Down, Power_IsOn, Power_IsOff, Reload, Download, Image, Save,
		Muted_Dis, UnMuted_Dis, Up_Dis, Down_Dis, Power_IsOn_Dis, Power_IsOff_Dis, Reload_Dis, Download_Dis, Image_Dis, Save_Dis,
		;
		public Icon getIcon() { return iconSource.getCachedIcon(this); }
		private static IconSource.CachedIcons<GrayCommandIcons> iconSource = IconSource.createCachedIcons(16, 16, 10, "GeneralIcons.GrayCommandIcons.png", GrayCommandIcons.values());
	}
	
}
