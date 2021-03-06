<template>
  <b-card v-if="selected">
    <template v-slot:header>
      <h3>
        <opensilex-Icon icon="ik#ik-map" />
        {{$t("component.infrastructure.facilities")}}
      </h3>
      <div class="card-header-right">
        <opensilex-CreateButton
          v-if="user.hasCredential(credentials.CREDENTIAL_INFRASTRUCTURE_MODIFICATION_ID)"
          @click="facilityForm.showCreateForm()"
          label="component.infrastructure.facility.add"
        ></opensilex-CreateButton>
      </div>
    </template>

    <b-table
      striped
      hover
      small
      responsive
      sort-by="typeLabel"
      :items="selected.facilities"
      :fields="fields"
    >
      <template v-slot:head(name)="data">{{$t(data.label)}}</template>
      <template v-slot:head(typeLabel)="data">{{$t(data.label)}}</template>
      <template v-slot:head(actions)="data">{{$t(data.label)}}</template>

      <template v-slot:cell(name)="data">
        <opensilex-Icon :icon="$opensilex.getRDFIcon(data.item.type)" />&nbsp;
        <span class="capitalize-first-letter">{{data.item.name}}</span>
      </template>

      <template v-slot:cell(typeLabel)="data">
        <span class="capitalize-first-letter">{{data.item.typeLabel}}</span>
      </template>

      <template v-slot:cell(actions)="data">
        <b-button-group class="tree-button-group" size="sm">
          <opensilex-EditButton
            v-if="user.hasCredential(credentials.CREDENTIAL_INFRASTRUCTURE_MODIFICATION_ID)"
            @click="facilityForm.showEditForm(data.item)"
            label="component.infrastructure.facility.update"
            :small="true"
          ></opensilex-EditButton>
          <opensilex-DeleteButton
            v-if="user.hasCredential(credentials.CREDENTIAL_INFRASTRUCTURE_MODIFICATION_ID)"
            @click="deleteFacility(data.item.uri)"
            label="component.infrastructure.facility.delete"
            :small="true"
          ></opensilex-DeleteButton>
        </b-button-group>
      </template>
    </b-table>

    <opensilex-ModalForm
      v-if="user.hasCredential(credentials.CREDENTIAL_INFRASTRUCTURE_MODIFICATION_ID)"
      ref="facilityForm"
      component="opensilex-InfrastructureFacilityForm"
      createTitle="component.infrastructure.facility.add"
      editTitle="component.infrastructure.facility.update"
      icon="ik#ik-map"
      @onCreate="$emit('onCreate', $event)"
      @onUpdate="$emit('onUpdate', $event)"
      :initForm="setInfrastructure"
    ></opensilex-ModalForm>
  </b-card>
</template>

<script lang="ts">
import { Component, Prop, Ref } from "vue-property-decorator";
import Vue from "vue";
import HttpResponse, { OpenSilexResponse } from "../../lib/HttpResponse";
import {
  InfrastructuresService,
  ResourceTreeDTO,
  InfrastructureGetDTO,
  InfrastructureFacilityGetDTO,
  InfrastructureTeamDTO
} from "opensilex-core/index";
import { GroupCreationDTO, GroupUpdateDTO } from "opensilex-security/index";

@Component
export default class InfrastructureFacilitiesView extends Vue {
  $opensilex: any;

  @Ref("facilityForm") readonly facilityForm!: any;

  get user() {
    return this.$store.state.user;
  }

  get credentials() {
    return this.$store.state.credentials;
  }

  @Prop()
  private selected: InfrastructureGetDTO;

  fields = [
    {
      key: "name",
      label: "component.common.name",
      sortable: true
    },
    {
      key: "typeLabel",
      label: "component.common.type",
      sortable: true
    },
    {
      label: "component.common.actions",
      key: "actions"
    }
  ];

  public deleteFacility(uri) {
    this.$opensilex
      .getService("opensilex-core.InfrastructuresService")
      .deleteInfrastructureFacility(uri)
      .then(() => {
        this.$emit("onDelete", uri);
      });
  }

  setInfrastructure(form) {
    form.infrastructure = this.selected.uri;
  }
}
</script>

<style scoped lang="scss">
</style>

