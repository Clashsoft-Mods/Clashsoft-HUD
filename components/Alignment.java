package clashsoft.mods.cshud.components;

public class Alignment
{
	public static final int		HORIZONTAL_LEFT		= 0;
	public static final int		HORIZONTAL_CENTER	= 1;
	public static final int		HORIZONTAL_RIGHT	= 2;
	
	public static final int		VERTICAL_TOP		= 0;
	public static final int		VERTICAL_CENTER		= 1;
	public static final int		VERTICAL_BOTTOM		= 2;
	
	public static final String	VERTICAL_CHARS		= "tcb";
	public static final String	HORIZONTAL_CHARS	= "lcr";
	
	public static Alignment		NONE				= new Alignment(0, 0)
													{
														@Override
														public int getX(int width, int screenWidth)
														{
															return -screenWidth;
														}
														
														@Override
														public int getY(int height, int screenHeight)
														{
															return -screenHeight;
														}
													};
	
	public static Alignment		TOP_LEFT			= new Alignment(0, 0);
	public static Alignment		TOP_CENTER			= new Alignment(0, 1);
	public static Alignment		TOP_RIGHT			= new Alignment(0, 2);
	
	public static Alignment		CENTER_LEFT			= new Alignment(1, 0);
	public static Alignment		CENTER				= new Alignment(1, 1);
	public static Alignment		CENTER_RIGHT		= new Alignment(1, 2);
	
	public static Alignment		BOTTOM_LEFT			= new Alignment(2, 0);
	public static Alignment		BOTTOM_CENTER		= new Alignment(2, 1);
	public static Alignment		BOTTOM_RIGHT		= new Alignment(2, 2);
	
	public int					vertical;
	public int					horizontal;
	
	public Alignment(int vertical, int horizontal)
	{
		if (vertical < 0 | vertical > 2)
		{
			throw new IllegalArgumentException("Invalid vertical alignment!");
		}
		if (horizontal < 0 | horizontal > 2)
		{
			throw new IllegalArgumentException("Invalid horizontal alignment!");
		}
		
		this.vertical = vertical;
		this.horizontal = horizontal;
	}
	
	public boolean isVerticallyCentered()
	{
		return this.vertical == 1;
	}
	
	public boolean isHorizontallyCentered()
	{
		return this.horizontal == 1;
	}
	
	public int getX(int width, int screenWidth)
	{
		switch (this.horizontal)
		{
		case HORIZONTAL_LEFT:
			return 0;
		case HORIZONTAL_CENTER:
			return (screenWidth - width) / 2;
		case HORIZONTAL_RIGHT:
			return screenWidth - width;
		default:
			return 0;
		}
	}
	
	public int getY(int height, int screenHeight)
	{
		switch (this.vertical)
		{
		case VERTICAL_TOP:
			return 0;
		case VERTICAL_CENTER:
			return (screenHeight - height) / 2;
		case VERTICAL_BOTTOM:
			return screenHeight - height;
		default:
			return 0;
		}
	}
	
	@Override
	public String toString()
	{
		return VERTICAL_CHARS.charAt(this.vertical) + "" + HORIZONTAL_CHARS.charAt(this.horizontal);
	}
	
	public static Alignment parseAlignment(String string)
	{
		string = string.toLowerCase();
		string = string.replace("top", "t").replace("center", "c").replace("bottom", "b").replace("left", "l").replace("right", "r");
		
		if ("tl".equals(string) || "lt".equals(string))
		{
			return TOP_LEFT;
		}
		else if ("tc".equals(string) || "ct".equals(string))
		{
			return TOP_CENTER;
		}
		else if ("tr".equals(string) || "rt".equals(string))
		{
			return TOP_RIGHT;
		}
		else if ("cl".equals(string) || "lc".equals(string))
		{
			return CENTER_LEFT;
		}
		else if ("cc".equals(string) || "c".equals(string))
		{
			return CENTER;
		}
		else if ("cr".equals(string) || "rc".equals(string))
		{
			return CENTER_RIGHT;
		}
		else if ("bl".equals(string) || "lb".equals(string))
		{
			return BOTTOM_LEFT;
		}
		else if ("bc".equals(string) || "cb".equals(string))
		{
			return BOTTOM_CENTER;
		}
		else if ("br".equals(string) || "rb".equals(string))
		{
			return BOTTOM_RIGHT;
		}
		else
		{
			return NONE;
		}
	}
}
