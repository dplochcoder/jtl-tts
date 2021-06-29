package lua.tts;

import lua.Native;
import lua.NativePrototype;

public class Color {
  public float r;
  public float g;
  public float b;
  public float a;

  public Color(float r, float g, float b, float a) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }

  public Color(float r, float g, float b) {
    this(r, g, b, 1.0f);
  }
  
  public static Color hex(int r, int g, int b, int a) {
    return new Color(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
  }
  
  public static Color hex(int r, int g, int b) {
    return hex(r, g, b, 255);
  }
  
  @NativePrototype
  public Color lerp(Color other, float ratio) { return null; }
  
  @Native(value = "$0 == $1")
  public static boolean approximatelyEqual(Color c1, Color c2) { return false; }
}