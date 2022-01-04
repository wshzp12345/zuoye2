CREATE TABLE `bank`.`account` (
  `name` VARCHAR(32) NOT NULL,
  `cardNo` VARCHAR(45) NOT NULL,
  `money` INT NULL,
  UNIQUE INDEX `name_UNIQUE` (`name` ASC) VISIBLE,
  UNIQUE INDEX `cardNo_UNIQUE` (`cardNo` ASC) VISIBLE,
  PRIMARY KEY (`cardNo`));