package lua.tts;

/**
 * Top-level program in a TableTopSImulator game.
 * 
 * <p><code>@Override</code> the methods in this class to define TTS-recognized globals
 */
public interface Program {
  default void onObjectSpawn(GameObject obj) {}
  
  default void onUpdate() {}
}