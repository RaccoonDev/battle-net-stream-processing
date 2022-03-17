/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.devraccoon.starcraft.domain.server;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class GameStarted extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -7677970091093989468L;


  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"GameStarted\",\"namespace\":\"com.devraccoon.starcraft.domain.server\",\"fields\":[{\"name\":\"eventTime\",\"type\":{\"type\":\"long\",\"logicalType\":\"timestamp-millis\"}},{\"name\":\"gameId\",\"type\":{\"type\":\"string\",\"logicalType\":\"UUID\"}},{\"name\":\"playerIds\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"logicalType\":\"UUID\"}}},{\"name\":\"mapId\",\"type\":\"string\"},{\"name\":\"regionId\",\"type\":{\"type\":\"string\",\"logicalType\":\"UUID\"}},{\"name\":\"gameType\",\"type\":{\"type\":\"enum\",\"name\":\"GameType\",\"namespace\":\"com.devraccoon.starcraft.domain.game\",\"symbols\":[\"FourVsFour\",\"OneVsOne\",\"ThreeVsThree\",\"TwoVsTwo\"]}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static final SpecificData MODEL$ = new SpecificData();
  static {
    MODEL$.addLogicalTypeConversion(new org.apache.avro.data.TimeConversions.TimestampMillisConversion());
  }

  private static final BinaryMessageEncoder<GameStarted> ENCODER =
      new BinaryMessageEncoder<GameStarted>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<GameStarted> DECODER =
      new BinaryMessageDecoder<GameStarted>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<GameStarted> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<GameStarted> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<GameStarted> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<GameStarted>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this GameStarted to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a GameStarted from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a GameStarted instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static GameStarted fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  private java.time.Instant eventTime;
  private java.lang.CharSequence gameId;
  private java.util.List<java.lang.CharSequence> playerIds;
  private java.lang.CharSequence mapId;
  private java.lang.CharSequence regionId;
  private com.devraccoon.starcraft.domain.game.GameType gameType;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public GameStarted() {}

  /**
   * All-args constructor.
   * @param eventTime The new value for eventTime
   * @param gameId The new value for gameId
   * @param playerIds The new value for playerIds
   * @param mapId The new value for mapId
   * @param regionId The new value for regionId
   * @param gameType The new value for gameType
   */
  public GameStarted(java.time.Instant eventTime, java.lang.CharSequence gameId, java.util.List<java.lang.CharSequence> playerIds, java.lang.CharSequence mapId, java.lang.CharSequence regionId, com.devraccoon.starcraft.domain.game.GameType gameType) {
    this.eventTime = eventTime.truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
    this.gameId = gameId;
    this.playerIds = playerIds;
    this.mapId = mapId;
    this.regionId = regionId;
    this.gameType = gameType;
  }

  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return eventTime;
    case 1: return gameId;
    case 2: return playerIds;
    case 3: return mapId;
    case 4: return regionId;
    case 5: return gameType;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  private static final org.apache.avro.Conversion<?>[] conversions =
      new org.apache.avro.Conversion<?>[] {
      new org.apache.avro.data.TimeConversions.TimestampMillisConversion(),
      null,
      null,
      null,
      null,
      null,
      null
  };

  @Override
  public org.apache.avro.Conversion<?> getConversion(int field) {
    return conversions[field];
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: eventTime = (java.time.Instant)value$; break;
    case 1: gameId = (java.lang.CharSequence)value$; break;
    case 2: playerIds = (java.util.List<java.lang.CharSequence>)value$; break;
    case 3: mapId = (java.lang.CharSequence)value$; break;
    case 4: regionId = (java.lang.CharSequence)value$; break;
    case 5: gameType = (com.devraccoon.starcraft.domain.game.GameType)value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'eventTime' field.
   * @return The value of the 'eventTime' field.
   */
  public java.time.Instant getEventTime() {
    return eventTime;
  }


  /**
   * Sets the value of the 'eventTime' field.
   * @param value the value to set.
   */
  public void setEventTime(java.time.Instant value) {
    this.eventTime = value.truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
  }

  /**
   * Gets the value of the 'gameId' field.
   * @return The value of the 'gameId' field.
   */
  public java.lang.CharSequence getGameId() {
    return gameId;
  }


  /**
   * Sets the value of the 'gameId' field.
   * @param value the value to set.
   */
  public void setGameId(java.lang.CharSequence value) {
    this.gameId = value;
  }

  /**
   * Gets the value of the 'playerIds' field.
   * @return The value of the 'playerIds' field.
   */
  public java.util.List<java.lang.CharSequence> getPlayerIds() {
    return playerIds;
  }


  /**
   * Sets the value of the 'playerIds' field.
   * @param value the value to set.
   */
  public void setPlayerIds(java.util.List<java.lang.CharSequence> value) {
    this.playerIds = value;
  }

  /**
   * Gets the value of the 'mapId' field.
   * @return The value of the 'mapId' field.
   */
  public java.lang.CharSequence getMapId() {
    return mapId;
  }


  /**
   * Sets the value of the 'mapId' field.
   * @param value the value to set.
   */
  public void setMapId(java.lang.CharSequence value) {
    this.mapId = value;
  }

  /**
   * Gets the value of the 'regionId' field.
   * @return The value of the 'regionId' field.
   */
  public java.lang.CharSequence getRegionId() {
    return regionId;
  }


  /**
   * Sets the value of the 'regionId' field.
   * @param value the value to set.
   */
  public void setRegionId(java.lang.CharSequence value) {
    this.regionId = value;
  }

  /**
   * Gets the value of the 'gameType' field.
   * @return The value of the 'gameType' field.
   */
  public com.devraccoon.starcraft.domain.game.GameType getGameType() {
    return gameType;
  }


  /**
   * Sets the value of the 'gameType' field.
   * @param value the value to set.
   */
  public void setGameType(com.devraccoon.starcraft.domain.game.GameType value) {
    this.gameType = value;
  }

  /**
   * Creates a new GameStarted RecordBuilder.
   * @return A new GameStarted RecordBuilder
   */
  public static com.devraccoon.starcraft.domain.server.GameStarted.Builder newBuilder() {
    return new com.devraccoon.starcraft.domain.server.GameStarted.Builder();
  }

  /**
   * Creates a new GameStarted RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new GameStarted RecordBuilder
   */
  public static com.devraccoon.starcraft.domain.server.GameStarted.Builder newBuilder(com.devraccoon.starcraft.domain.server.GameStarted.Builder other) {
    if (other == null) {
      return new com.devraccoon.starcraft.domain.server.GameStarted.Builder();
    } else {
      return new com.devraccoon.starcraft.domain.server.GameStarted.Builder(other);
    }
  }

  /**
   * Creates a new GameStarted RecordBuilder by copying an existing GameStarted instance.
   * @param other The existing instance to copy.
   * @return A new GameStarted RecordBuilder
   */
  public static com.devraccoon.starcraft.domain.server.GameStarted.Builder newBuilder(com.devraccoon.starcraft.domain.server.GameStarted other) {
    if (other == null) {
      return new com.devraccoon.starcraft.domain.server.GameStarted.Builder();
    } else {
      return new com.devraccoon.starcraft.domain.server.GameStarted.Builder(other);
    }
  }

  /**
   * RecordBuilder for GameStarted instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<GameStarted>
    implements org.apache.avro.data.RecordBuilder<GameStarted> {

    private java.time.Instant eventTime;
    private java.lang.CharSequence gameId;
    private java.util.List<java.lang.CharSequence> playerIds;
    private java.lang.CharSequence mapId;
    private java.lang.CharSequence regionId;
    private com.devraccoon.starcraft.domain.game.GameType gameType;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$, MODEL$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.devraccoon.starcraft.domain.server.GameStarted.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.eventTime)) {
        this.eventTime = data().deepCopy(fields()[0].schema(), other.eventTime);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.gameId)) {
        this.gameId = data().deepCopy(fields()[1].schema(), other.gameId);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.playerIds)) {
        this.playerIds = data().deepCopy(fields()[2].schema(), other.playerIds);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
      if (isValidValue(fields()[3], other.mapId)) {
        this.mapId = data().deepCopy(fields()[3].schema(), other.mapId);
        fieldSetFlags()[3] = other.fieldSetFlags()[3];
      }
      if (isValidValue(fields()[4], other.regionId)) {
        this.regionId = data().deepCopy(fields()[4].schema(), other.regionId);
        fieldSetFlags()[4] = other.fieldSetFlags()[4];
      }
      if (isValidValue(fields()[5], other.gameType)) {
        this.gameType = data().deepCopy(fields()[5].schema(), other.gameType);
        fieldSetFlags()[5] = other.fieldSetFlags()[5];
      }
    }

    /**
     * Creates a Builder by copying an existing GameStarted instance
     * @param other The existing instance to copy.
     */
    private Builder(com.devraccoon.starcraft.domain.server.GameStarted other) {
      super(SCHEMA$, MODEL$);
      if (isValidValue(fields()[0], other.eventTime)) {
        this.eventTime = data().deepCopy(fields()[0].schema(), other.eventTime);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.gameId)) {
        this.gameId = data().deepCopy(fields()[1].schema(), other.gameId);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.playerIds)) {
        this.playerIds = data().deepCopy(fields()[2].schema(), other.playerIds);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.mapId)) {
        this.mapId = data().deepCopy(fields()[3].schema(), other.mapId);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.regionId)) {
        this.regionId = data().deepCopy(fields()[4].schema(), other.regionId);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.gameType)) {
        this.gameType = data().deepCopy(fields()[5].schema(), other.gameType);
        fieldSetFlags()[5] = true;
      }
    }

    /**
      * Gets the value of the 'eventTime' field.
      * @return The value.
      */
    public java.time.Instant getEventTime() {
      return eventTime;
    }


    /**
      * Sets the value of the 'eventTime' field.
      * @param value The value of 'eventTime'.
      * @return This builder.
      */
    public com.devraccoon.starcraft.domain.server.GameStarted.Builder setEventTime(java.time.Instant value) {
      validate(fields()[0], value);
      this.eventTime = value.truncatedTo(java.time.temporal.ChronoUnit.MILLIS);
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'eventTime' field has been set.
      * @return True if the 'eventTime' field has been set, false otherwise.
      */
    public boolean hasEventTime() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'eventTime' field.
      * @return This builder.
      */
    public com.devraccoon.starcraft.domain.server.GameStarted.Builder clearEventTime() {
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'gameId' field.
      * @return The value.
      */
    public java.lang.CharSequence getGameId() {
      return gameId;
    }


    /**
      * Sets the value of the 'gameId' field.
      * @param value The value of 'gameId'.
      * @return This builder.
      */
    public com.devraccoon.starcraft.domain.server.GameStarted.Builder setGameId(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.gameId = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'gameId' field has been set.
      * @return True if the 'gameId' field has been set, false otherwise.
      */
    public boolean hasGameId() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'gameId' field.
      * @return This builder.
      */
    public com.devraccoon.starcraft.domain.server.GameStarted.Builder clearGameId() {
      gameId = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'playerIds' field.
      * @return The value.
      */
    public java.util.List<java.lang.CharSequence> getPlayerIds() {
      return playerIds;
    }


    /**
      * Sets the value of the 'playerIds' field.
      * @param value The value of 'playerIds'.
      * @return This builder.
      */
    public com.devraccoon.starcraft.domain.server.GameStarted.Builder setPlayerIds(java.util.List<java.lang.CharSequence> value) {
      validate(fields()[2], value);
      this.playerIds = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'playerIds' field has been set.
      * @return True if the 'playerIds' field has been set, false otherwise.
      */
    public boolean hasPlayerIds() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'playerIds' field.
      * @return This builder.
      */
    public com.devraccoon.starcraft.domain.server.GameStarted.Builder clearPlayerIds() {
      playerIds = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'mapId' field.
      * @return The value.
      */
    public java.lang.CharSequence getMapId() {
      return mapId;
    }


    /**
      * Sets the value of the 'mapId' field.
      * @param value The value of 'mapId'.
      * @return This builder.
      */
    public com.devraccoon.starcraft.domain.server.GameStarted.Builder setMapId(java.lang.CharSequence value) {
      validate(fields()[3], value);
      this.mapId = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'mapId' field has been set.
      * @return True if the 'mapId' field has been set, false otherwise.
      */
    public boolean hasMapId() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'mapId' field.
      * @return This builder.
      */
    public com.devraccoon.starcraft.domain.server.GameStarted.Builder clearMapId() {
      mapId = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'regionId' field.
      * @return The value.
      */
    public java.lang.CharSequence getRegionId() {
      return regionId;
    }


    /**
      * Sets the value of the 'regionId' field.
      * @param value The value of 'regionId'.
      * @return This builder.
      */
    public com.devraccoon.starcraft.domain.server.GameStarted.Builder setRegionId(java.lang.CharSequence value) {
      validate(fields()[4], value);
      this.regionId = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'regionId' field has been set.
      * @return True if the 'regionId' field has been set, false otherwise.
      */
    public boolean hasRegionId() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'regionId' field.
      * @return This builder.
      */
    public com.devraccoon.starcraft.domain.server.GameStarted.Builder clearRegionId() {
      regionId = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'gameType' field.
      * @return The value.
      */
    public com.devraccoon.starcraft.domain.game.GameType getGameType() {
      return gameType;
    }


    /**
      * Sets the value of the 'gameType' field.
      * @param value The value of 'gameType'.
      * @return This builder.
      */
    public com.devraccoon.starcraft.domain.server.GameStarted.Builder setGameType(com.devraccoon.starcraft.domain.game.GameType value) {
      validate(fields()[5], value);
      this.gameType = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'gameType' field has been set.
      * @return True if the 'gameType' field has been set, false otherwise.
      */
    public boolean hasGameType() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'gameType' field.
      * @return This builder.
      */
    public com.devraccoon.starcraft.domain.server.GameStarted.Builder clearGameType() {
      gameType = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GameStarted build() {
      try {
        GameStarted record = new GameStarted();
        record.eventTime = fieldSetFlags()[0] ? this.eventTime : (java.time.Instant) defaultValue(fields()[0]);
        record.gameId = fieldSetFlags()[1] ? this.gameId : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.playerIds = fieldSetFlags()[2] ? this.playerIds : (java.util.List<java.lang.CharSequence>) defaultValue(fields()[2]);
        record.mapId = fieldSetFlags()[3] ? this.mapId : (java.lang.CharSequence) defaultValue(fields()[3]);
        record.regionId = fieldSetFlags()[4] ? this.regionId : (java.lang.CharSequence) defaultValue(fields()[4]);
        record.gameType = fieldSetFlags()[5] ? this.gameType : (com.devraccoon.starcraft.domain.game.GameType) defaultValue(fields()[5]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<GameStarted>
    WRITER$ = (org.apache.avro.io.DatumWriter<GameStarted>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<GameStarted>
    READER$ = (org.apache.avro.io.DatumReader<GameStarted>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}










