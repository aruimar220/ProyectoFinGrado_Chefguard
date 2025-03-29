-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 29-03-2025 a las 19:54:03
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `chefguard`
--

DELIMITER $$
--
-- Procedimientos
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `ActualizarStock` (IN `p_ID_alimento` INT, IN `p_cantidad` INT)   BEGIN
    -- Evitar stock negativo
    IF (SELECT cantidad_alimento FROM alimentos WHERE ID_alimento = p_ID_alimento) + p_cantidad < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede tener cantidad negativa en stock';
    END IF;

    -- Actualizar stock
    UPDATE alimentos 
    SET cantidad_alimento = cantidad_alimento + p_cantidad
    WHERE ID_alimento = p_ID_alimento;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `EliminarAlimentosCaducados` ()   BEGIN
    DELETE FROM alimentos
    WHERE fecha_caducidad < CURDATE() AND cantidad_alimento = 0;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `GenerarAlertasCaducidad` ()   BEGIN
    -- Insertar alertas para alimentos que caducan en 3 días
    INSERT INTO alertas (ID_alimento, mensaje, fecha_generada)
    SELECT ID_alimento, CONCAT('El alimento "', nombre_alimento, '" está por caducar'), NOW()
    FROM alimentos
    WHERE fecha_caducidad BETWEEN CURDATE() AND CURDATE() + INTERVAL 3 DAY;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `InsertarAlimento` (IN `p_nombre_alimento` VARCHAR(30), IN `p_fecha_consumo_preferente` DATE, IN `p_fecha_caducidad` DATE, IN `p_cantidad_alimento` INT, IN `p_lote` VARCHAR(30), IN `p_ID_proveedor` INT, IN `p_ID_tipo_alimento` INT, IN `p_ID_ambiente` INT)   BEGIN
    -- Verificar existencia de proveedor, tipo de alimento y ambiente
    IF NOT EXISTS (SELECT 1 FROM proveedor WHERE ID_proveedor = p_ID_proveedor) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Proveedor no existe';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM tipo_alimento WHERE ID_tipo_alimento = p_ID_tipo_alimento) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tipo de alimento no existe';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM ambiente WHERE ID_ambiente = p_ID_ambiente) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ambiente no existe';
    END IF;

    -- Insertar el alimento
    INSERT INTO alimentos (nombre_alimento, fecha_consumo_preferente, fecha_caducidad, cantidad_alimento, lote, ID_proveedor, ID_tipo_alimento, ID_ambiente)
    VALUES (p_nombre_alimento, p_fecha_consumo_preferente, p_fecha_caducidad, p_cantidad_alimento, p_lote, p_ID_proveedor, p_ID_tipo_alimento, p_ID_ambiente);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `InsertarAmbiente` (IN `p_nombre` VARCHAR(25), IN `p_ID_tipo_ambiente` INT)   BEGIN
    -- Validar existencia del tipo de ambiente
    IF NOT EXISTS (SELECT 1 FROM tipo_ambiente WHERE ID_tipo_ambiente = p_ID_tipo_ambiente) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El tipo de ambiente no existe';
    END IF;
    
    -- Insertar ambiente
    INSERT INTO ambiente (nombre, ID_tipo_ambiente)
    VALUES (p_nombre, p_ID_tipo_ambiente);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `InsertarProveedor` (IN `p_nombre` VARCHAR(25), IN `p_telefono` VARCHAR(15), IN `p_correo` VARCHAR(100), IN `p_direccion` VARCHAR(100))   BEGIN
    -- Verificar que el correo del proveedor no exista
    IF EXISTS (SELECT 1 FROM proveedor WHERE correo = p_correo) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El proveedor ya está registrado';
    END IF;
    
    -- Insertar proveedor
    INSERT INTO proveedor (nombre, telefono, correo, direccion)
    VALUES (p_nombre, p_telefono, p_correo, p_direccion);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `InsertarTipoAlimento` (IN `p_nombre` VARCHAR(25))   BEGIN
    -- Insertar tipo de alimento
    INSERT INTO tipo_alimento (nombre) VALUES (p_nombre);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `InsertarUsuario` (IN `p_nombre_usuario` VARCHAR(25), IN `p_correo` VARCHAR(100), IN `p_password_hash` VARCHAR(255))   BEGIN
    -- Verificar que el correo no esté registrado
    IF EXISTS (SELECT 1 FROM usuario WHERE correo = p_correo) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El correo ya está registrado';
    END IF;
    
    -- Insertar usuario
    INSERT INTO usuario (nombre_usuario, correo, password_hash)
    VALUES (p_nombre_usuario, p_correo, p_password_hash);
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `InventarioPorTipo` ()   BEGIN
    SELECT t.nombre AS TipoAlimento, COUNT(a.ID_alimento) AS TotalAlimentos
    FROM alimentos a
    JOIN tipo_alimento t ON a.ID_tipo_alimento = t.ID_tipo_alimento
    GROUP BY t.nombre;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `RegistrarConsumo` (IN `p_ID_alimento` INT, IN `p_cantidad_usada` INT, IN `p_estado_consumo` ENUM('Consumido','Parcial','Desperdiciado'), IN `p_ID_usuario` INT)   BEGIN
    -- Verificar que la cantidad a consumir no sea mayor a la disponible
    IF (SELECT cantidad_alimento FROM alimentos WHERE ID_alimento = p_ID_alimento) < p_cantidad_usada THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No hay suficiente cantidad disponible';
    END IF;

    -- Registrar el consumo
    INSERT INTO registro_de_consumo (ID_alimento, cantidad_usada, estado_consumo, ID_usuario)
    VALUES (p_ID_alimento, p_cantidad_usada, p_estado_consumo, p_ID_usuario);

    -- Actualizar stock
    UPDATE alimentos
    SET cantidad_alimento = cantidad_alimento - p_cantidad_usada
    WHERE ID_alimento = p_ID_alimento;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `alertas`
--

CREATE TABLE `alertas` (
  `ID_alerta` int(11) NOT NULL,
  `ID_alimento` int(11) NOT NULL,
  `mensaje` text NOT NULL,
  `fecha_generada` datetime DEFAULT current_timestamp(),
  `leida` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `alertas`
--

INSERT INTO `alertas` (`ID_alerta`, `ID_alimento`, `mensaje`, `fecha_generada`, `leida`) VALUES
(1, 1, '?La leche est? pr?xima a caducar!', '2025-05-12 08:00:00', 0),
(2, 2, 'Carne de res a punto de caducar. ?sala pronto.', '2025-04-18 09:00:00', 0);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `alimentos`
--

CREATE TABLE `alimentos` (
  `ID_alimento` int(11) NOT NULL,
  `nombre_alimento` varchar(30) NOT NULL,
  `fecha_consumo_preferente` date DEFAULT NULL,
  `fecha_caducidad` date NOT NULL,
  `cantidad_alimento` int(11) NOT NULL CHECK (`cantidad_alimento` >= 0),
  `lote` varchar(30) NOT NULL,
  `estado` enum('Disponible','Agotado','Caducado') DEFAULT 'Disponible',
  `ID_proveedor` int(11) DEFAULT NULL,
  `ID_tipo_alimento` int(11) DEFAULT NULL,
  `ID_ambiente` int(11) DEFAULT NULL,
  `fecha_registro` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `alimentos`
--

INSERT INTO `alimentos` (`ID_alimento`, `nombre_alimento`, `fecha_consumo_preferente`, `fecha_caducidad`, `cantidad_alimento`, `lote`, `estado`, `ID_proveedor`, `ID_tipo_alimento`, `ID_ambiente`, `fecha_registro`) VALUES
(1, 'Leche', '2025-05-10', '2025-05-15', 11, 'L001', 'Disponible', 1, 2, 1, '2025-03-29 18:55:51'),
(2, 'Carne de res', NULL, '2025-04-20', 5, 'C002', 'Disponible', 2, 1, 2, '2025-03-29 18:55:51'),
(3, 'Manzanas', '2025-06-01', '2025-06-10', 20, 'M003', 'Disponible', 3, 4, 3, '2025-03-29 18:55:51'),
(4, 'Arroz', NULL, '2026-12-31', 50, 'A004', 'Disponible', 1, 5, 3, '2025-03-29 18:55:51'),
(5, 'Cerveza', NULL, '2026-08-01', 30, 'B005', 'Disponible', 2, 6, 3, '2025-03-29 18:55:51');

--
-- Disparadores `alimentos`
--
DELIMITER $$
CREATE TRIGGER `Actualizar_Estado_Alimento` BEFORE UPDATE ON `alimentos` FOR EACH ROW BEGIN
    IF NEW.fecha_caducidad < CURDATE() THEN
        SET NEW.estado = 'Caducado';
    ELSEIF NEW.cantidad_alimento = 0 THEN
        SET NEW.estado = 'Agotado';
    ELSE
        SET NEW.estado = 'Disponible';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `Avisar_Caducidad_Anticipada` AFTER INSERT ON `alimentos` FOR EACH ROW BEGIN
    IF NEW.fecha_caducidad BETWEEN CURDATE() AND CURDATE() + INTERVAL 3 DAY THEN
        INSERT INTO alertas (ID_alimento, mensaje, fecha_generada)
        VALUES (NEW.ID_alimento, CONCAT('El alimento "', NEW.nombre_alimento, '" caduca en menos de 3 días.'));
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `Avisar_Caducidad_Temprana` AFTER INSERT ON `alimentos` FOR EACH ROW BEGIN
    IF DATEDIFF(NEW.fecha_caducidad, CURDATE()) <= 7 THEN
        INSERT INTO alertas (ID_alimento, mensaje, fecha_generada)
        VALUES (NEW.ID_alimento, CONCAT('El alimento "', NEW.nombre_alimento, '" tiene una caducidad temprana. Se recomienda su uso pronto.'));
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `Eliminar_Alimentos_Caducados` AFTER UPDATE ON `alimentos` FOR EACH ROW BEGIN
    IF NEW.fecha_caducidad < CURDATE() AND NEW.cantidad_alimento = 0 THEN
        DELETE FROM alimentos WHERE ID_alimento = NEW.ID_alimento;
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `Evitar_Cantidad_Negativa` BEFORE INSERT ON `alimentos` FOR EACH ROW BEGIN
    IF NEW.cantidad_alimento < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se pueden registrar alimentos con cantidad negativa.';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `Evitar_Stock_Negativo` BEFORE UPDATE ON `alimentos` FOR EACH ROW BEGIN
    IF NEW.cantidad_alimento < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede tener stock negativo.';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `Impedir_Caducidad_Pasada` BEFORE INSERT ON `alimentos` FOR EACH ROW BEGIN
    IF NEW.fecha_caducidad < CURDATE() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No se puede registrar un alimento con fecha de caducidad pasada';
    END IF;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `Verificar_Proveedor` BEFORE INSERT ON `alimentos` FOR EACH ROW BEGIN
    IF NEW.ID_proveedor IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Cada alimento debe tener un proveedor asignado.';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `ambiente`
--

CREATE TABLE `ambiente` (
  `ID_ambiente` int(11) NOT NULL,
  `nombre` varchar(25) NOT NULL,
  `ID_tipo_ambiente` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `ambiente`
--

INSERT INTO `ambiente` (`ID_ambiente`, `nombre`, `ID_tipo_ambiente`) VALUES
(1, 'Nevera', 1),
(2, 'Congelador', 2),
(3, 'Estanter?a', 3);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `proveedor`
--

CREATE TABLE `proveedor` (
  `ID_proveedor` int(11) NOT NULL,
  `nombre` varchar(25) NOT NULL,
  `telefono` varchar(15) DEFAULT NULL,
  `correo` varchar(100) DEFAULT NULL,
  `direccion` varchar(100) DEFAULT NULL,
  `fecha_registro` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `proveedor`
--

INSERT INTO `proveedor` (`ID_proveedor`, `nombre`, `telefono`, `correo`, `direccion`, `fecha_registro`) VALUES
(1, 'Proveedor A', '123456789', 'proveedorA@email.com', 'Calle 123, Ciudad', '2025-03-29 18:55:51'),
(2, 'Proveedor B', '987654321', 'proveedorB@email.com', 'Avenida 456, Ciudad', '2025-03-29 18:55:51'),
(3, 'Proveedor C', '112233445', 'proveedorC@email.com', 'Plaza 789, Ciudad', '2025-03-29 18:55:51');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `registro_de_consumo`
--

CREATE TABLE `registro_de_consumo` (
  `ID_registro` int(11) NOT NULL,
  `ID_alimento` int(11) NOT NULL,
  `fecha_uso` datetime DEFAULT current_timestamp(),
  `cantidad_usada` int(11) NOT NULL CHECK (`cantidad_usada` > 0),
  `estado_consumo` enum('Consumido','Parcial','Desperdiciado') NOT NULL,
  `ID_usuario` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `registro_de_consumo`
--

INSERT INTO `registro_de_consumo` (`ID_registro`, `ID_alimento`, `fecha_uso`, `cantidad_usada`, `estado_consumo`, `ID_usuario`) VALUES
(1, 1, '2025-04-15 12:30:00', 2, 'Parcial', 1),
(2, 2, '2025-04-16 14:00:00', 1, 'Consumido', 2),
(3, 3, '2025-04-17 10:15:00', 5, 'Consumido', 3);

--
-- Disparadores `registro_de_consumo`
--
DELIMITER $$
CREATE TRIGGER `Actualizar_Cantidad_Alimento` AFTER INSERT ON `registro_de_consumo` FOR EACH ROW BEGIN
    UPDATE alimentos
    SET cantidad_alimento = cantidad_alimento - NEW.cantidad_usada
    WHERE ID_alimento = NEW.ID_alimento;
END
$$
DELIMITER ;
DELIMITER $$
CREATE TRIGGER `Actualizar_Estado_Consumo` BEFORE INSERT ON `registro_de_consumo` FOR EACH ROW BEGIN
    IF NEW.cantidad_usada >= (SELECT cantidad_alimento FROM alimentos WHERE ID_alimento = NEW.ID_alimento) THEN
        SET NEW.estado_consumo = 'Consumido';
    ELSEIF NEW.cantidad_usada < (SELECT cantidad_alimento FROM alimentos WHERE ID_alimento = NEW.ID_alimento) THEN
        SET NEW.estado_consumo = 'Parcial';
    ELSE
        SET NEW.estado_consumo = 'Desperdiciado';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tipo_alimento`
--

CREATE TABLE `tipo_alimento` (
  `ID_tipo_alimento` int(11) NOT NULL,
  `nombre` varchar(25) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `tipo_alimento`
--

INSERT INTO `tipo_alimento` (`ID_tipo_alimento`, `nombre`) VALUES
(1, 'Carnes'),
(2, 'L?cteos'),
(3, 'Verduras'),
(4, 'Frutas'),
(5, 'Cereales'),
(6, 'Bebidas');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `tipo_ambiente`
--

CREATE TABLE `tipo_ambiente` (
  `ID_tipo_ambiente` int(11) NOT NULL,
  `nombre` varchar(25) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `tipo_ambiente`
--

INSERT INTO `tipo_ambiente` (`ID_tipo_ambiente`, `nombre`) VALUES
(1, 'Refrigerado'),
(2, 'Congelado'),
(3, 'Temperatura Ambiente');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario`
--

CREATE TABLE `usuario` (
  `ID_usuario` int(11) NOT NULL,
  `nombre_usuario` varchar(25) NOT NULL,
  `correo` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `fecha_registro` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuario`
--

INSERT INTO `usuario` (`ID_usuario`, `nombre_usuario`, `correo`, `password_hash`, `fecha_registro`) VALUES
(1, 'usuario1', 'usuario1@email.com', 'hash1', '2025-03-29 18:55:51'),
(2, 'usuario2', 'usuario2@email.com', 'hash2', '2025-03-29 18:55:51'),
(3, 'usuario3', 'usuario3@email.com', 'hash3', '2025-03-29 18:55:51');

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `vista_alertas_pendientes`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `vista_alertas_pendientes` (
`ID_alerta` int(11)
,`nombre_alimento` varchar(30)
,`mensaje` text
,`fecha_generada` datetime
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `vista_alimentos_ambiente`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `vista_alimentos_ambiente` (
`ID_alimento` int(11)
,`nombre_alimento` varchar(30)
,`ambiente` varchar(25)
,`tipo_ambiente` varchar(25)
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `vista_alimentos_desperdiciados`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `vista_alimentos_desperdiciados` (
`ID_registro` int(11)
,`nombre_usuario` varchar(25)
,`nombre_alimento` varchar(30)
,`cantidad_usada` int(11)
,`fecha_uso` datetime
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `vista_alimentos_por_proveedor`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `vista_alimentos_por_proveedor` (
`proveedor` varchar(25)
,`ID_alimento` int(11)
,`nombre_alimento` varchar(30)
,`cantidad_alimento` int(11)
,`estado` enum('Disponible','Agotado','Caducado')
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `vista_alimentos_proximos_caducar`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `vista_alimentos_proximos_caducar` (
`ID_alimento` int(11)
,`nombre_alimento` varchar(30)
,`fecha_caducidad` date
,`dias_restantes` int(7)
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `vista_consumo_usuarios`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `vista_consumo_usuarios` (
`ID_registro` int(11)
,`nombre_usuario` varchar(25)
,`nombre_alimento` varchar(30)
,`cantidad_usada` int(11)
,`estado_consumo` enum('Consumido','Parcial','Desperdiciado')
,`fecha_uso` datetime
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `vista_detalle_alimentos`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `vista_detalle_alimentos` (
`ID_alimento` int(11)
,`nombre_alimento` varchar(30)
,`fecha_consumo_preferente` date
,`fecha_caducidad` date
,`cantidad_alimento` int(11)
,`lote` varchar(30)
,`estado` enum('Disponible','Agotado','Caducado')
,`proveedor` varchar(25)
,`ambiente` varchar(25)
,`tipo_alimento` varchar(25)
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `vista_estadisticas_consumo`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `vista_estadisticas_consumo` (
`nombre_alimento` varchar(30)
,`total_consumos` bigint(21)
,`total_cantidad_consumida` decimal(32,0)
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `vista_inventario_disponible`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `vista_inventario_disponible` (
`ID_alimento` int(11)
,`nombre_alimento` varchar(30)
,`cantidad_alimento` int(11)
,`estado` enum('Disponible','Agotado','Caducado')
);

-- --------------------------------------------------------

--
-- Estructura Stand-in para la vista `vista_usuarios_registro_consumo`
-- (Véase abajo para la vista actual)
--
CREATE TABLE `vista_usuarios_registro_consumo` (
`nombre_usuario` varchar(25)
,`nombre_alimento` varchar(30)
,`cantidad_usada` int(11)
,`estado_consumo` enum('Consumido','Parcial','Desperdiciado')
,`fecha_uso` datetime
);

-- --------------------------------------------------------

--
-- Estructura para la vista `vista_alertas_pendientes`
--
DROP TABLE IF EXISTS `vista_alertas_pendientes`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_alertas_pendientes`  AS SELECT `al`.`ID_alerta` AS `ID_alerta`, `a`.`nombre_alimento` AS `nombre_alimento`, `al`.`mensaje` AS `mensaje`, `al`.`fecha_generada` AS `fecha_generada` FROM (`alertas` `al` join `alimentos` `a` on(`al`.`ID_alimento` = `a`.`ID_alimento`)) WHERE `al`.`leida` = 0 ;

-- --------------------------------------------------------

--
-- Estructura para la vista `vista_alimentos_ambiente`
--
DROP TABLE IF EXISTS `vista_alimentos_ambiente`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_alimentos_ambiente`  AS SELECT `a`.`ID_alimento` AS `ID_alimento`, `a`.`nombre_alimento` AS `nombre_alimento`, `am`.`nombre` AS `ambiente`, `ta`.`nombre` AS `tipo_ambiente` FROM ((`alimentos` `a` join `ambiente` `am` on(`a`.`ID_ambiente` = `am`.`ID_ambiente`)) join `tipo_ambiente` `ta` on(`am`.`ID_tipo_ambiente` = `ta`.`ID_tipo_ambiente`)) ;

-- --------------------------------------------------------

--
-- Estructura para la vista `vista_alimentos_desperdiciados`
--
DROP TABLE IF EXISTS `vista_alimentos_desperdiciados`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_alimentos_desperdiciados`  AS SELECT `rc`.`ID_registro` AS `ID_registro`, `u`.`nombre_usuario` AS `nombre_usuario`, `a`.`nombre_alimento` AS `nombre_alimento`, `rc`.`cantidad_usada` AS `cantidad_usada`, `rc`.`fecha_uso` AS `fecha_uso` FROM ((`registro_de_consumo` `rc` join `usuario` `u` on(`rc`.`ID_usuario` = `u`.`ID_usuario`)) join `alimentos` `a` on(`rc`.`ID_alimento` = `a`.`ID_alimento`)) WHERE `rc`.`estado_consumo` = 'Desperdiciado' ;

-- --------------------------------------------------------

--
-- Estructura para la vista `vista_alimentos_por_proveedor`
--
DROP TABLE IF EXISTS `vista_alimentos_por_proveedor`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_alimentos_por_proveedor`  AS SELECT `p`.`nombre` AS `proveedor`, `a`.`ID_alimento` AS `ID_alimento`, `a`.`nombre_alimento` AS `nombre_alimento`, `a`.`cantidad_alimento` AS `cantidad_alimento`, `a`.`estado` AS `estado` FROM (`alimentos` `a` join `proveedor` `p` on(`a`.`ID_proveedor` = `p`.`ID_proveedor`)) ;

-- --------------------------------------------------------

--
-- Estructura para la vista `vista_alimentos_proximos_caducar`
--
DROP TABLE IF EXISTS `vista_alimentos_proximos_caducar`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_alimentos_proximos_caducar`  AS SELECT `alimentos`.`ID_alimento` AS `ID_alimento`, `alimentos`.`nombre_alimento` AS `nombre_alimento`, `alimentos`.`fecha_caducidad` AS `fecha_caducidad`, to_days(`alimentos`.`fecha_caducidad`) - to_days(curdate()) AS `dias_restantes` FROM `alimentos` WHERE `alimentos`.`fecha_caducidad` between curdate() and curdate() + interval 7 day ;

-- --------------------------------------------------------

--
-- Estructura para la vista `vista_consumo_usuarios`
--
DROP TABLE IF EXISTS `vista_consumo_usuarios`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_consumo_usuarios`  AS SELECT `rc`.`ID_registro` AS `ID_registro`, `u`.`nombre_usuario` AS `nombre_usuario`, `a`.`nombre_alimento` AS `nombre_alimento`, `rc`.`cantidad_usada` AS `cantidad_usada`, `rc`.`estado_consumo` AS `estado_consumo`, `rc`.`fecha_uso` AS `fecha_uso` FROM ((`registro_de_consumo` `rc` join `usuario` `u` on(`rc`.`ID_usuario` = `u`.`ID_usuario`)) join `alimentos` `a` on(`rc`.`ID_alimento` = `a`.`ID_alimento`)) ;

-- --------------------------------------------------------

--
-- Estructura para la vista `vista_detalle_alimentos`
--
DROP TABLE IF EXISTS `vista_detalle_alimentos`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_detalle_alimentos`  AS SELECT `a`.`ID_alimento` AS `ID_alimento`, `a`.`nombre_alimento` AS `nombre_alimento`, `a`.`fecha_consumo_preferente` AS `fecha_consumo_preferente`, `a`.`fecha_caducidad` AS `fecha_caducidad`, `a`.`cantidad_alimento` AS `cantidad_alimento`, `a`.`lote` AS `lote`, `a`.`estado` AS `estado`, `p`.`nombre` AS `proveedor`, `am`.`nombre` AS `ambiente`, `ta`.`nombre` AS `tipo_alimento` FROM (((`alimentos` `a` left join `proveedor` `p` on(`a`.`ID_proveedor` = `p`.`ID_proveedor`)) left join `ambiente` `am` on(`a`.`ID_ambiente` = `am`.`ID_ambiente`)) left join `tipo_alimento` `ta` on(`a`.`ID_tipo_alimento` = `ta`.`ID_tipo_alimento`)) ;

-- --------------------------------------------------------

--
-- Estructura para la vista `vista_estadisticas_consumo`
--
DROP TABLE IF EXISTS `vista_estadisticas_consumo`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_estadisticas_consumo`  AS SELECT `a`.`nombre_alimento` AS `nombre_alimento`, count(`rc`.`ID_registro`) AS `total_consumos`, sum(`rc`.`cantidad_usada`) AS `total_cantidad_consumida` FROM (`registro_de_consumo` `rc` join `alimentos` `a` on(`rc`.`ID_alimento` = `a`.`ID_alimento`)) GROUP BY `a`.`nombre_alimento` ;

-- --------------------------------------------------------

--
-- Estructura para la vista `vista_inventario_disponible`
--
DROP TABLE IF EXISTS `vista_inventario_disponible`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_inventario_disponible`  AS SELECT `alimentos`.`ID_alimento` AS `ID_alimento`, `alimentos`.`nombre_alimento` AS `nombre_alimento`, `alimentos`.`cantidad_alimento` AS `cantidad_alimento`, `alimentos`.`estado` AS `estado` FROM `alimentos` WHERE `alimentos`.`estado` = 'Disponible' ;

-- --------------------------------------------------------

--
-- Estructura para la vista `vista_usuarios_registro_consumo`
--
DROP TABLE IF EXISTS `vista_usuarios_registro_consumo`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vista_usuarios_registro_consumo`  AS SELECT `u`.`nombre_usuario` AS `nombre_usuario`, `a`.`nombre_alimento` AS `nombre_alimento`, `rc`.`cantidad_usada` AS `cantidad_usada`, `rc`.`estado_consumo` AS `estado_consumo`, `rc`.`fecha_uso` AS `fecha_uso` FROM ((`registro_de_consumo` `rc` join `usuario` `u` on(`rc`.`ID_usuario` = `u`.`ID_usuario`)) join `alimentos` `a` on(`rc`.`ID_alimento` = `a`.`ID_alimento`)) ;

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `alertas`
--
ALTER TABLE `alertas`
  ADD PRIMARY KEY (`ID_alerta`),
  ADD KEY `ID_alimento` (`ID_alimento`);

--
-- Indices de la tabla `alimentos`
--
ALTER TABLE `alimentos`
  ADD PRIMARY KEY (`ID_alimento`),
  ADD KEY `ID_proveedor` (`ID_proveedor`),
  ADD KEY `ID_tipo_alimento` (`ID_tipo_alimento`),
  ADD KEY `ID_ambiente` (`ID_ambiente`);

--
-- Indices de la tabla `ambiente`
--
ALTER TABLE `ambiente`
  ADD PRIMARY KEY (`ID_ambiente`),
  ADD KEY `ID_tipo_ambiente` (`ID_tipo_ambiente`);

--
-- Indices de la tabla `proveedor`
--
ALTER TABLE `proveedor`
  ADD PRIMARY KEY (`ID_proveedor`),
  ADD UNIQUE KEY `correo` (`correo`);

--
-- Indices de la tabla `registro_de_consumo`
--
ALTER TABLE `registro_de_consumo`
  ADD PRIMARY KEY (`ID_registro`),
  ADD KEY `ID_alimento` (`ID_alimento`),
  ADD KEY `ID_usuario` (`ID_usuario`);

--
-- Indices de la tabla `tipo_alimento`
--
ALTER TABLE `tipo_alimento`
  ADD PRIMARY KEY (`ID_tipo_alimento`);

--
-- Indices de la tabla `tipo_ambiente`
--
ALTER TABLE `tipo_ambiente`
  ADD PRIMARY KEY (`ID_tipo_ambiente`);

--
-- Indices de la tabla `usuario`
--
ALTER TABLE `usuario`
  ADD PRIMARY KEY (`ID_usuario`),
  ADD UNIQUE KEY `nombre_usuario` (`nombre_usuario`),
  ADD UNIQUE KEY `correo` (`correo`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `alertas`
--
ALTER TABLE `alertas`
  MODIFY `ID_alerta` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT de la tabla `alimentos`
--
ALTER TABLE `alimentos`
  MODIFY `ID_alimento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT de la tabla `ambiente`
--
ALTER TABLE `ambiente`
  MODIFY `ID_ambiente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `proveedor`
--
ALTER TABLE `proveedor`
  MODIFY `ID_proveedor` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `registro_de_consumo`
--
ALTER TABLE `registro_de_consumo`
  MODIFY `ID_registro` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `tipo_alimento`
--
ALTER TABLE `tipo_alimento`
  MODIFY `ID_tipo_alimento` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT de la tabla `tipo_ambiente`
--
ALTER TABLE `tipo_ambiente`
  MODIFY `ID_tipo_ambiente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `usuario`
--
ALTER TABLE `usuario`
  MODIFY `ID_usuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `alertas`
--
ALTER TABLE `alertas`
  ADD CONSTRAINT `alertas_ibfk_1` FOREIGN KEY (`ID_alimento`) REFERENCES `alimentos` (`ID_alimento`) ON DELETE CASCADE;

--
-- Filtros para la tabla `alimentos`
--
ALTER TABLE `alimentos`
  ADD CONSTRAINT `alimentos_ibfk_1` FOREIGN KEY (`ID_proveedor`) REFERENCES `proveedor` (`ID_proveedor`) ON DELETE SET NULL,
  ADD CONSTRAINT `alimentos_ibfk_2` FOREIGN KEY (`ID_tipo_alimento`) REFERENCES `tipo_alimento` (`ID_tipo_alimento`) ON DELETE SET NULL,
  ADD CONSTRAINT `alimentos_ibfk_3` FOREIGN KEY (`ID_ambiente`) REFERENCES `ambiente` (`ID_ambiente`) ON DELETE SET NULL;

--
-- Filtros para la tabla `ambiente`
--
ALTER TABLE `ambiente`
  ADD CONSTRAINT `ambiente_ibfk_1` FOREIGN KEY (`ID_tipo_ambiente`) REFERENCES `tipo_ambiente` (`ID_tipo_ambiente`) ON DELETE SET NULL;

--
-- Filtros para la tabla `registro_de_consumo`
--
ALTER TABLE `registro_de_consumo`
  ADD CONSTRAINT `registro_de_consumo_ibfk_1` FOREIGN KEY (`ID_alimento`) REFERENCES `alimentos` (`ID_alimento`) ON DELETE CASCADE,
  ADD CONSTRAINT `registro_de_consumo_ibfk_2` FOREIGN KEY (`ID_usuario`) REFERENCES `usuario` (`ID_usuario`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
