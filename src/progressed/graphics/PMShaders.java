package progressed.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class PMShaders{
    public static MaterializeShader materializeShader;
    public static VerticalBuildShader vertBuild;
    public static BlockBuildCenteShader blockBuildCenter;

    public static void init(){
        materializeShader = new MaterializeShader();
        vertBuild = new VerticalBuildShader();
        blockBuildCenter = new BlockBuildCenteShader();
    }

    public static class MaterializeShader extends PMLoadShader{
        public float progress, offset;
        public Color color = new Color();
        public TextureRegion region;

        MaterializeShader(){
            super("materialize");
        }

        @Override
        public void apply(){
            setUniformf("u_color", color);
            setUniformf("u_progress", progress);
            setUniformf("u_offset", offset);
            setUniformf("u_uv", region.u, region.v);
            setUniformf("u_uv2", region.u2, region.v2);
            setUniformf("u_texsize", region.texture.width, region.texture.height);
        }
    }

    public static class VerticalBuildShader extends PMLoadShader{
        public float progress, time;
        public Color color = new Color();
        public TextureRegion region;

        public VerticalBuildShader(){
            super("vertbuild");
        }

        @Override
        public void apply(){
            setUniformf("u_time", time);
            setUniformf("u_color", color);
            setUniformf("u_progress", progress);
            setUniformf("u_uv", region.u, region.v);
            setUniformf("u_uv2", region.u2, region.v2);
            setUniformf("u_texsize", region.texture.width, region.texture.height);
        }
    }

    public static class BlockBuildCenteShader extends PMLoadShader{
        public float progress;
        public TextureRegion region = new TextureRegion();
        public float time;

        BlockBuildCenteShader(){
            super("blockbuildcenter");
        }

        @Override
        public void apply(){
            setUniformf("u_progress", progress);
            setUniformf("u_uv", region.u, region.v);
            setUniformf("u_uv2", region.u2, region.v2);
            setUniformf("u_time", time);
            setUniformf("u_texsize", region.texture.width, region.texture.height);
        }
    }

    static class PMLoadShader extends Shader{
        PMLoadShader(String frag){
            super(
                files.internal("shaders/default.vert"),
                tree.get("shaders/" + frag + ".frag")
            );
        }
    }
}
