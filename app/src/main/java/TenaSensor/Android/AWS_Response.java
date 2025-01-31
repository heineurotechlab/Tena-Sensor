package TenaSensor.Android;

/**
 * @author Amir Modan (amir5modan@gmail.com)
 * Class which contains the object that handles Responses made to the app by AWS
 *
 * Data received from AWS include:
 *  speed (Float)
 *  smoothness (Float)
 *  time (Float)
 */

public class AWS_Response {
    float speed, smoothness, time;

    public float getSpeed() { return speed; }

    public float getSmoothness() { return smoothness; }

    public float getTime() { return time; }

    public void setStats(float speed, float smoothness, float time) {
        this.speed = speed;
        this.smoothness = smoothness;
        this.time = time;
    }

    public AWS_Response(float speed, float smoothness, float time) {
        this.speed = speed;
        this.smoothness = smoothness;
        this.time = time;
    }

    public AWS_Response() {
    }
}
