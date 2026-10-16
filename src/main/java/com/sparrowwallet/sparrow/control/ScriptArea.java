package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.protocol.Script;
import com.sparrowwallet.drongo.protocol.ScriptChunk;
import javafx.geometry.Pos;
import org.controlsfx.control.decoration.Decorator;
import org.controlsfx.control.decoration.GraphicDecoration;
import org.fxmisc.richtext.CodeArea;

public class ScriptArea extends CodeArea {
    private Script script;

    public void appendScript(Script script) {
        appendScript(script, null, null);
    }

    public void appendScript(Script script, Script redeemScript, Script witnessScript) {
        if(this.script == null) {
            this.script = script;
            ScriptContextMenu contextMenu = new ScriptContextMenu(this, script);
            setContextMenu(contextMenu);
        }

        ScriptDisplay matched = null;
        for(ScriptDisplay display : ScriptDisplay.kinds()) {
            if(display.matches(script)) {
                matched = display;
                break;
            }
        }
        if(matched != null) {
            matched.append(this, script);
        } else {
            int[] pubkeyAndSignature = {1, 1};
            for (int i = 0; i < script.getChunks().size(); i++) {
                ScriptChunk chunk = script.getChunks().get(i);
                ScriptDisplay.ScriptChunkDisplay.of(chunk).append(this, chunk, redeemScript, witnessScript, pubkeyAndSignature);
                if(i < script.getChunks().size() - 1) {
                    append(" ", "");
                }
            }
        }
    }

    public void clear() {
        super.clear();
        this.script = null;
        setDisable(false);
        setContextMenu(null);
        Decorator.removeAllDecorations(this);
    }

    public void addPSBTDecoration(String description, String styleClass) {
        Decorator.addDecoration(this, new GraphicDecoration(new TextDecoration("PSBT", description, styleClass), Pos.TOP_RIGHT));
    }

    public Script getScript() {
        return script;
    }
}
