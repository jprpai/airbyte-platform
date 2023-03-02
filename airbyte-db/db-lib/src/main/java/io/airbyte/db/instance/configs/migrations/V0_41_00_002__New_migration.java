/*
 * Copyright (c) 2023 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.db.instance.configs.migrations;

import static org.jooq.impl.DSL.currentOffsetDateTime;
import static org.jooq.impl.DSL.foreignKey;
import static org.jooq.impl.DSL.primaryKey;
import static org.jooq.impl.DSL.unique;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.JSONB;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: update migration description in the class name
public class V0_41_00_002__New_migration extends BaseJavaMigration {

  private static final Logger LOGGER = LoggerFactory.getLogger(V0_41_00_002__New_migration.class);

  @Override
  public void migrate(final Context context) throws Exception {
    LOGGER.info("Running migration: {}", this.getClass().getSimpleName());

    // Warning: please do not use any jOOQ generated code to write a migration.
    // As database schema changes, the generated jOOQ code can be deprecated. So
    // old migration may not compile if there is any generated code.
    try (final DSLContext ctx = DSL.using(context.getConnection())) {
      addDeclarativeManifestTable(ctx);
      addActiveDeclarativeManifestTable(ctx);
    }
  }

  private static void addDeclarativeManifestTable(final DSLContext ctx) {
    final Field<UUID> actorDefinitionId = DSL.field("actor_definition_id", SQLDataType.UUID.nullable(false));
    final Field<String> description = DSL.field("description", SQLDataType.VARCHAR(256).nullable(false));
    final Field<JSONB> manifest = DSL.field("manifest", SQLDataType.JSONB.nullable(false));
    final Field<JSONB> spec = DSL.field("spec", SQLDataType.JSONB.nullable(false));
    final Field<Long> version = DSL.field("version", SQLDataType.BIGINT.nullable(false));
    final Field<OffsetDateTime> createdAt =
        DSL.field("created_at", SQLDataType.TIMESTAMPWITHTIMEZONE.nullable(false).defaultValue(currentOffsetDateTime()));

    ctx.createTableIfNotExists("declarative_manifest")
        .columns(actorDefinitionId, description, manifest, spec, version, createdAt)
        .constraints(primaryKey(actorDefinitionId, version)).execute();
  }

  private static void addActiveDeclarativeManifestTable(final DSLContext ctx) {
    final Field<UUID> id = DSL.field("id", SQLDataType.UUID.nullable(false));
    final Field<UUID> actorDefinitionId = DSL.field("actor_definition_id", SQLDataType.UUID.nullable(false));
    final Field<Long> version = DSL.field("version", SQLDataType.BIGINT.nullable(false));
    final Field<OffsetDateTime> createdAt =
        DSL.field("created_at", SQLDataType.TIMESTAMPWITHTIMEZONE.nullable(false).defaultValue(currentOffsetDateTime()));
    final Field<OffsetDateTime> updatedAt =
        DSL.field("updated_at", SQLDataType.TIMESTAMPWITHTIMEZONE.nullable(false).defaultValue(currentOffsetDateTime()));

    ctx.createTableIfNotExists("active_declarative_manifest")
        .columns(id, actorDefinitionId, version, createdAt, updatedAt)
        .constraints(
            primaryKey(id),
            foreignKey(actorDefinitionId, version).references("declarative_manifest", "actor_definition_id", "version"),
            unique(actorDefinitionId))
        .execute();
  }

}
