package com.sparrowwallet.sparrow.control;

import com.sparrowwallet.drongo.protocol.ModernSig;
import com.sparrowwallet.drongo.protocol.Script;
import com.sparrowwallet.drongo.protocol.ScriptChunk;
import com.sparrowwallet.drongo.protocol.ScriptType;

import java.util.List;

interface ScriptDisplay {
    boolean matches(Script script);

    void append(ScriptArea area, Script script);

    static List<ScriptDisplay> kinds() {
        return List.of(
                new OpcodeHashDisplay(ScriptType.P2WPKH, "<wpkh>"),
                new ModernSingleDisplay(),
                new ModernPolicyDisplay(),
                new P2pkhDisplay(),
                new P2shDisplay(),
                new OpcodeHashDisplay(ScriptType.P2WSH, "<wsh>")
        );
    }

    record OpcodeHashDisplay(ScriptType type, String placeholder) implements ScriptDisplay {
        @Override
        public boolean matches(Script script) {
            return type.isScriptType(script);
        }

        @Override
        public void append(ScriptArea area, Script script) {
            area.append(script.getChunks().get(0).toString(), "script-opcode");
            area.append(" ", "");
            area.append(placeholder, "script-hash");
        }
    }

    final class ModernSingleDisplay implements ScriptDisplay {
        @Override
        public boolean matches(Script script) {
            return ModernSig.matchSingle(script) != null;
        }

        @Override
        public void append(ScriptArea area, Script script) {
            List<ScriptChunk> chunks = script.getChunks();
            area.append("<fp>", "script-hash");
            for(int i = 1; i < chunks.size(); i++) {
                area.append(" ", "");
                area.append(chunks.get(i).toString(), "script-opcode");
            }
        }
    }

    final class ModernPolicyDisplay implements ScriptDisplay {
        @Override
        public boolean matches(Script script) {
            return ModernSig.matchPolicy(script) != null;
        }

        @Override
        public void append(ScriptArea area, Script script) {
            ModernSig.Policy policy = ModernSig.matchPolicy(script);
            List<ScriptChunk> chunks = script.getChunks();
            area.append(chunks.get(0).toString(), "script-opcode");
            for(int i = 0; i < policy.n(); i++) {
                area.append(" ", "");
                area.append("<hash>", "script-hash");
            }
            for(int i = 1 + policy.n(); i < chunks.size(); i++) {
                area.append(" ", "");
                area.append(chunks.get(i).toString(), "script-opcode");
            }
        }
    }

    final class P2shDisplay implements ScriptDisplay {
        @Override
        public boolean matches(Script script) {
            return ScriptType.P2SH.isScriptType(script);
        }

        @Override
        public void append(ScriptArea area, Script script) {
            area.append(script.getChunks().get(0).toString(), "script-opcode");
            area.append(" ", "");
            area.append("<sh>", "script-hash");
            area.append(" ", "");
            area.append(script.getChunks().get(2).toString(), "script-opcode");
        }
    }

    final class P2pkhDisplay implements ScriptDisplay {
        @Override
        public boolean matches(Script script) {
            return ScriptType.P2PKH.isScriptType(script);
        }

        @Override
        public void append(ScriptArea area, Script script) {
            area.append(script.getChunks().get(0).toString(), "script-opcode");
            area.append(" ", "");
            area.append(script.getChunks().get(1).toString(), "script-opcode");
            area.append(" ", "");
            area.append("<pkh>", "script-hash");
            area.append(" ", "");
            area.append(script.getChunks().get(3).toString(), "script-opcode");
            area.append(" ", "");
            area.append(script.getChunks().get(4).toString(), "script-opcode");
        }
    }
}
