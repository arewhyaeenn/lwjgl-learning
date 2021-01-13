precision mediump float;

// data type definitions
struct DirectionalLight
{
    vec3 direction; // xyz direction of light
    vec3 ambient; // rgb contribution to scene ambient light
    vec3 diffuse; // rgb intensity of diffuse
    vec3 specular; // rgb intensity of specular
};

struct PointLight
{
    vec3 position; // xyz position of source
    vec3 ambient; // rgb contribution to scene ambient light
    vec3 diffuse; // rgb intensity of diffuse
    vec3 specular; // rgb intensity of specular
};

struct Material
{
    float diffuse; // diffuse reflection constant
    float specular; // specular reflection constant
    float ambient; // ambient reflection constant
    float shininess; // shininess constant
};

struct Camera
{
    vec3 position;
    mat4 mProjView;
};

struct DiffuseSpecularDouble {
    vec3 diffuse;
    vec3 specular;
};

// lights
uniform vec3 ambientLight;
uniform DirectionalLight directionalLights[16];
uniform PointLight pointLights[16];

// material
uniform Material material;

// camera
uniform Camera cam;

// surface
varying vec3 fragPosition;
varying vec3 fragNormal;

// texture
varying vec2 fragTexCoord;
uniform sampler2D sampler;

vec3 directionalLightDiffuse(DirectionalLight light, float slDotN) {
    return slDotN * light.diffuse;
}

vec3 directionalLightSpecular(DirectionalLight light, vec3 sl, float slDotN, vec3 v) {
    vec3 illumination = vec3(0);
    vec3 rl = 2.0 * slDotN * fragNormal - sl;
    float rlDotV = dot(rl, v);
    if (rlDotV > 0.0) {
        illumination = pow(rlDotV, material.shininess) * light.specular;
    }
    return illumination;
}

DiffuseSpecularDouble directional(DirectionalLight light, vec3 v) {
    DiffuseSpecularDouble illumination = DiffuseSpecularDouble(vec3(0), vec3(0));

    vec3 sl = -light.direction;
    float slDotN = dot(sl, fragNormal);
    if (slDotN > 0.0) {
        illumination.diffuse = directionalLightDiffuse(light, slDotN);
        illumination.specular = directionalLightSpecular(light, sl, slDotN, v);
    }

    return illumination;
}

vec3 pointLightDiffuse(PointLight light, float slDotN, float squaredDistance) {
    return (slDotN * light.diffuse) / squaredDistance;
}

vec3 pointLightSpecular(PointLight light, vec3 sl, float slDotN, vec3 v, float squaredDistance) {
    vec3 illumination = vec3(0);

    vec3 rl = 2.0 * slDotN * fragNormal - sl;
    float rlDotV = dot(rl, v);
    if (rlDotV > 0.0) {
        illumination = (pow(rlDotV, material.shininess) * light.specular) / squaredDistance;
    }

    return illumination;
}

DiffuseSpecularDouble point(PointLight light, vec3 v) {
    DiffuseSpecularDouble illumination = DiffuseSpecularDouble(vec3(0), vec3(0));

    vec3 sl = normalize(light.position - fragPosition);
    float slDotN = dot(sl, fragNormal);
    vec3 diff = fragPosition - light.position;
    float squaredDistance = max(dot(diff, diff), 1.0);
    if (slDotN > 0.0) {
        illumination.diffuse = pointLightDiffuse(light, slDotN, squaredDistance);
        illumination.specular = pointLightSpecular(light, sl, slDotN, v, squaredDistance);
    }

    return illumination;
}

void main()
{
    vec4 texel = texture2D(sampler, fragTexCoord);

    vec3 ambientLightSum = ambientLight;

    DirectionalLight directional_light;
    PointLight point_light;

    vec3 v = normalize(cam.position - fragPosition);

    vec3 diffuseLightSum = vec3(0);
    vec3 specularLightSum = vec3(0);

    DiffuseSpecularDouble directionalPerLight;
    DiffuseSpecularDouble pointPerLight;

    for (int i = 0; i < 16; ++i) {
        directional_light = directionalLights[i];
        point_light = pointLights[i];
        ambientLightSum += directional_light.ambient + point_light.ambient;

        directionalPerLight = directional(directional_light, v);
        diffuseLightSum += directionalPerLight.diffuse;
        specularLightSum += directionalPerLight.specular;

        pointPerLight = point(point_light, v);
        diffuseLightSum += pointPerLight.diffuse;
        specularLightSum += pointPerLight.specular;

    }

    vec3 illumination = material.ambient * ambientLightSum +
    material.diffuse * diffuseLightSum +
    material.specular * specularLightSum;

    gl_FragColor = vec4(texel.rgb * illumination, texel.a);
}
