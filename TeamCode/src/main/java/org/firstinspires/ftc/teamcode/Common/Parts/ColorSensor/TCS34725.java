package org.firstinspires.ftc.teamcode.Common.Parts.ColorSensor;

import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;

/**
 * FTC-compatible driver for the TCS34725 RGB color sensor (I2C address 0x29).
 */
@I2cDeviceType
@DeviceProperties(
        name = "TCS34725 RGB Sensor",
        xmlTag = "TCS34725New"
)

public class TCS34725 extends I2cDeviceSynchDevice<I2cDeviceSynch> implements ColorSensor {

    // === I2C Constants ===
    private static final I2cAddr I2C_ADDRESS = I2cAddr.create7bit(0x29);

    // === Register Addresses ===
    private static final byte REGISTER_COMMAND = (byte) 0x80; // CMD bit + register address
    private static final byte REGISTER_ENABLE = 0x00;
    private static final byte REGISTER_ATIME = 0x01;
    private static final byte REGISTER_CONTROL = 0x0F;
    private static final byte REGISTER_ID = 0x12;
    private static final byte REGISTER_COLOR_DATA_START = 0x14;

    // === Bit Definitions ===
    private static final byte ENABLE_PON  = 0x1; // Power ON
    private static final byte ENABLE_AEN  = 0x2; // RGBC Enable

    private static final byte ID_TCS34725 = 0x44;
    private static final byte ID_TCS34727 = 0x4D;

    private static final byte CMD_AUTO_INCREMENT = (byte) 0xA0; // CMD + auto increment + register address[4:0]

    // ===============================================================
    // Constructor
    // ===============================================================
    public TCS34725(I2cDeviceSynch deviceClient, boolean deviceClientIsOwned) {
        //Legacy constructor
        super(deviceClient, deviceClientIsOwned);
        this.deviceClient.setI2cAddress(I2C_ADDRESS);
        deviceClient.engage();
    }

    @Override
    protected boolean doInitialize() {
        byte id = getID();

        //Power on & enable RGB
        writeRegister(REGISTER_ENABLE, ENABLE_PON);
        waitForInit();
        writeRegister(REGISTER_ENABLE, (byte) (ENABLE_PON | ENABLE_AEN));

        setIntegrationTime(100);//Cycle time 24 ms
        setGain(Gain.X60);

        return true;
    }

    private void waitForInit() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Sleep interrupted", e);
        }
    }

    private byte getID() {
        return readRegister(REGISTER_ID);
    }

    private void writeRegister(byte Register, byte value) {
        deviceClient.write(new byte[]{(byte) (REGISTER_COMMAND | Register), value},I2cWaitControl.WRITTEN);
    }

    private byte readRegister(byte Register) {
        deviceClient.write(new byte[]{(byte) (REGISTER_COMMAND | Register)},I2cWaitControl.WRITTEN);
        return deviceClient.read(1)[0];
    }

    private int[] getRawRGB() {
        // Read 8 bytes of raw color data
        byte cmd = (byte) (CMD_AUTO_INCREMENT | REGISTER_COLOR_DATA_START);
        deviceClient.write(new byte[]{cmd});
        byte[] data = deviceClient.read(8);

        int c = (data[1] & 0xFF) << 8 | (data[0] & 0xFF);
        int r = (data[3] & 0xFF) << 8 | (data[2] & 0xFF);
        int g = (data[5] & 0xFF) << 8 | (data[4] & 0xFF);
        int b = (data[7] & 0xFF) << 8 | (data[6] & 0xFF);
        return new int[]{r, g, b, c};

    }

    private void setIntegrationTime(byte ATIME) {
        writeRegister(REGISTER_ATIME, ATIME);
    }

    // ===============================================================
    // Public API
    // ===============================================================

    public enum Gain {
        X1((byte) 0x00),
        X4((byte) 0x01),
        X16((byte) 0x02),
        X60((byte) 0x03);

        public final byte regValue;
        Gain(byte val) { this.regValue = val; }
    }

    /**
    * Sets integration time
     * @param ms — time in milliseconds (between 2.4–700)
     */
    public void setIntegrationTime(int ms){
        double clampedTime = Math.max(2.4, Math.min(700.0, ms));
        int atime = (int) Math.round(256 - clampedTime / 2.4);
        atime = Math.max(0, Math.min(255, atime));
        writeRegister(REGISTER_ATIME, (byte) atime);
    }



    public void setGain(Gain gain) {
        writeRegister(REGISTER_CONTROL, gain.regValue);
    }

    public NormalizedRGBA getRGB() {
        int[] raw = getRawRGB();
        int r = raw[0], g = raw[1], b = raw[2], c = raw[3];

        if (c == 0) return new NormalizedRGBA();



        NormalizedRGBA RGB = new NormalizedRGBA();
        RGB.red = r;
        RGB.green = g;
        RGB.blue = b;
        RGB.alpha = c;


        return RGB;
    }

    // ===============================================================
    // Required overrides
    // ===============================================================

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Adafruit;
    }

    @Override
    public String getDeviceName() {
        return "TCS34725 RGB Sensor";
    }
}