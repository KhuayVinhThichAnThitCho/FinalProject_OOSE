declare module "json-bigint" {
  interface JSONbigOptions {
    storeAsString?: boolean;
    strict?: boolean;
    alwaysParseAsBig?: boolean;
    protoAction?: "error" | "ignore" | "preserve";
    constructorAction?: "error" | "ignore" | "preserve";
  }
  interface JSONbigInstance {
    parse: (text: string) => unknown;
    stringify: (value: unknown) => string;
  }
  function JSONbig(options?: JSONbigOptions): JSONbigInstance;
  export = JSONbig;
}
