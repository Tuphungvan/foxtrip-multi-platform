package haui.foxtrip.id;

import java.util.UUID;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.uuid.UuidValueGenerator;

import com.fasterxml.uuid.Generators;

public class UuidVersion7Generator implements UuidValueGenerator {

    @Override
    public UUID generateUuid(SharedSessionContractImplementor session) {
        return Generators.timeBasedEpochGenerator().generate();
    }
}
