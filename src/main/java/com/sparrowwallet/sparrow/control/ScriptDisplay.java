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

    interface ScriptChunkDisplay {
        boolean matches(ScriptChunk chunk);

        void append(ScriptArea area, ScriptChunk chunk, Script redeemScript, Script witnessScript, int[] pubkeyAndSignature);

        static List<ScriptChunkDisplay> kinds() {
            return List.of(
                    new OpcodeChunk(),
                    new PubKeyChunk(),
                    new SignatureChunk(),
                    new ControlBlockChunk(),
                    new StringChunk(),
                    new NestedScriptChunk(),
                    new OtherChunk()
            );
        }

        static ScriptChunkDisplay of(ScriptChunk chunk) {
            for(ScriptChunkDisplay display : kinds()) {
                if(display.matches(chunk)) {
                    return display;
                }
            }
            return new OtherChunk();
        }
    }

    final class OpcodeChunk implements ScriptChunkDisplay {
        @Override
        public boolean matches(ScriptChunk chunk) {
            return chunk.isOpCode();
        }

        @Override
        public void append(ScriptArea area, ScriptChunk chunk, Script redeemScript, Script witnessScript, int[] pubkeyAndSignature) {
            if(chunk.getOpcode() == com.sparrowwallet.drongo.protocol.ScriptOpCodes.OP_0 && witnessScript != null) {
                area.append("<empty>", "script-other");
            } else {
                area.append(chunk.toString(), "script-opcode");
            }
        }
    }

    final class PubKeyChunk implements ScriptChunkDisplay {
        @Override
        public boolean matches(ScriptChunk chunk) {
            return chunk.isPubKey();
        }

        @Override
        public void append(ScriptArea area, ScriptChunk chunk, Script redeemScript, Script witnessScript, int[] pubkeyAndSignature) {
            area.append("<pubkey" + pubkeyAndSignature[0]++ + ">", "script-pubkey");
        }
    }

    final class SignatureChunk implements ScriptChunkDisplay {
        @Override
        public boolean matches(ScriptChunk chunk) {
            return chunk.isSignature();
        }

        @Override
        public void append(ScriptArea area, ScriptChunk chunk, Script redeemScript, Script witnessScript, int[] pubkeyAndSignature) {
            area.append("<signature" + pubkeyAndSignature[1]++ + ">", "script-signature");
        }
    }

    final class ControlBlockChunk implements ScriptChunkDisplay {
        @Override
        public boolean matches(ScriptChunk chunk) {
            return chunk.isTaprootControlBlock();
        }

        @Override
        public void append(ScriptArea area, ScriptChunk chunk, Script redeemScript, Script witnessScript, int[] pubkeyAndSignature) {
            area.append("<controlblock>", "script-controlblock");
        }
    }

    final class StringChunk implements ScriptChunkDisplay {
        @Override
        public boolean matches(ScriptChunk chunk) {
            return chunk.isString();
        }

        @Override
        public void append(ScriptArea area, ScriptChunk chunk, Script redeemScript, Script witnessScript, int[] pubkeyAndSignature) {
            area.append(chunk.toString(), "script-other");
        }
    }

    final class NestedScriptChunk implements ScriptChunkDisplay {
        @Override
        public boolean matches(ScriptChunk chunk) {
            return chunk.isScript();
        }

        @Override
        public void append(ScriptArea area, ScriptChunk chunk, Script redeemScript, Script witnessScript, int[] pubkeyAndSignature) {
            Script nestedScript = chunk.getScript();
            if(nestedScript.equals(redeemScript)) {
                area.append("<RedeemScript>", "script-redeem");
            } else if(nestedScript.equals(witnessScript)) {
                area.append("<WitnessScript>", "script-redeem");
            } else {
                area.append("(", "script-nest");
                area.appendScript(nestedScript);
                area.append(")", "script-nest");
            }
        }
    }

    final class OtherChunk implements ScriptChunkDisplay {
        @Override
        public boolean matches(ScriptChunk chunk) {
            return true;
        }

        @Override
        public void append(ScriptArea area, ScriptChunk chunk, Script redeemScript, Script witnessScript, int[] pubkeyAndSignature) {
            area.append(chunk.toString(), "script-other");
        }
    }
}
