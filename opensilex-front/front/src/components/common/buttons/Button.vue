<template>
  <b-button
    @click.prevent="$emit('click')"
    :title="$t(label)"
    :size="small ? 'sm' : 'md'"
    :variant="variant"
    :disabled="disabled"
  >
    <slot name="icon">
      <opensilex-Icon :icon="icon" />
    </slot>
    <span class="button-label" v-if="!small">{{$t(label)}}</span>
  </b-button>
</template>

<script lang="ts">
import { Component, Prop } from "vue-property-decorator";
import Vue from "vue";

@Component
export default class Button extends Vue {
  @Prop()
  label: string;

  @Prop()
  variant: string;

  @Prop({
    default: true
  })
  small: boolean;

  @Prop({
    default: false
  })
  disabled: boolean;

  @Prop()
  icon: string;
}
</script>

<style scoped lang="scss">
.button-label {
  margin-left: 5px;
}
</style>

